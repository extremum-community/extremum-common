package io.extremum.mongo.springdata;

import io.extremum.common.reactive.ReactiveEventPublisher;
import io.extremum.mongo.springdata.lifecycle.ReactiveOrigin;
import io.extremum.sharedmodels.descriptor.Descriptor;
import org.bson.Document;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.FindPublisherPreparer;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.convert.MongoWriter;
import org.springframework.data.mongodb.core.mapping.event.AfterConvertEvent;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.data.mongodb.core.mapping.event.MongoMappingEvent;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * {@link ReactiveMongoTemplate} extension that handles events generation differently.
 *
 * 1. Standard spring-data-mongo events interesting for us are marked as 'reactive'
 * ones so that blocking-only event listeners can distinguish them to ignore them.
 * 2. Our own restricted set of reactive events are generated to support automatic
 * {@link Descriptor} creation for new model instances and model ID resolving from
 * descriptors. These events are not guaranteed to be generated for all possible
 * cases, just for the cases sufficient for our purposes.
 *
 * @author rpuch
 */
public class ReactiveMongoTemplateWithReactiveEvents extends ReactiveMongoTemplate {
    private final ReactiveEventPublisher reactiveEventPublisher;

    public ReactiveMongoTemplateWithReactiveEvents(ReactiveMongoDatabaseFactory mongoDatabaseFactory,
                                                   MongoConverter mongoConverter,
                                                   ReactiveEventPublisher reactiveEventPublisher) {
        super(mongoDatabaseFactory, mongoConverter);
        this.reactiveEventPublisher = reactiveEventPublisher;
    }

    @Override
    protected <E extends MongoMappingEvent<T>, T> E maybeEmitEvent(E event) {
        return super.maybeEmitEvent(maybeWrapInReactiveEventToAllowListenerKnowAboutReactiveness(event));
    }

    private <E extends MongoMappingEvent<T>, T> E maybeWrapInReactiveEventToAllowListenerKnowAboutReactiveness(
            E event) {
        if (event instanceof BeforeConvertEvent) {
            //noinspection unchecked
            event = (E) new BeforeConvertEventFromReactiveContext<>((BeforeConvertEvent<T>) event);
        }
        if (event instanceof AfterSaveEvent) {
            //noinspection unchecked
            event = (E) new AfterSaveEventFromReactiveContext<>((AfterSaveEvent<T>) event);
        }
        if (event instanceof AfterConvertEvent) {
            //noinspection unchecked
            event = (E) new AfterConvertEventFromReactiveContext<>((AfterConvertEvent<T>) event);
        }
        return event;
    }

    @Override
    protected <T> Mono<T> doInsert(String collectionName, T objectToSave, MongoWriter<Object> writer) {
        return wrapOneInBeforeConvertAndAfterSaveEvents(objectToSave,
                () -> super.doInsert(collectionName, objectToSave, writer));
    }

    private <T> Mono<T> wrapOneInBeforeConvertAndAfterSaveEvents(T objectToSave, SaveOne<T> saver) {
        return reactiveEventPublisher.publishEvent(new ReactiveBeforeConvertEvent<>(objectToSave))
                .then(saver.save())
                .flatMap(saved -> {
                    return reactiveEventPublisher.publishEvent(new ReactiveAfterSaveEvent<>(saved))
                            .thenReturn(saved);
                });
    }

    @Override
    protected <T> Flux<T> doInsertBatch(String collectionName, Collection<? extends T> batchToSave,
                                        MongoWriter<Object> writer) {
        return wrapManyInBeforeConvertAndAfterSaveEvents(batchToSave,
                () -> super.doInsertBatch(collectionName, batchToSave, writer));
    }

    private <T> Flux<T> wrapManyInBeforeConvertAndAfterSaveEvents(Iterable<? extends T> batchToSave,
                                                                  SaveMany<T> saver) {
        return Flux.fromIterable(batchToSave)
                .map(objectToSave -> reactiveEventPublisher.publishEvent(
                        new ReactiveBeforeConvertEvent<>(objectToSave)))
                .thenMany(saver.save())
                .concatMap(saved -> {
                    return reactiveEventPublisher.publishEvent(new ReactiveAfterSaveEvent<>(saved))
                            .thenReturn(saved);
                });
    }

    @Override
    protected <T> Mono<T> doSave(String collectionName, T objectToSave, MongoWriter<Object> writer) {
        return wrapOneInBeforeConvertAndAfterSaveEvents(objectToSave,
                () -> super.doSave(collectionName, objectToSave, writer));
    }

    @Override
    public <T> Flux<T> find(@Nullable Query query, Class<T> entityClass, String collectionName) {
        return super.find(query, entityClass, collectionName)
                .concatMap(loaded -> {
                    return reactiveEventPublisher.publishEvent(new ReactiveAfterConvertEvent<>(loaded))
                            .thenReturn(loaded);
                });
    }

    @Override
    protected <T> Mono<T> doFindOne(String collectionName, Document query, @Nullable Document fields,
                                    Class<T> entityClass, FindPublisherPreparer preparer) {
        Mono<T> result = super.doFindOne(collectionName, query, fields, entityClass, preparer);
        return wrapOneInAfterConvertEvent(result, collectionName);
    }

    private <T> Mono<T> wrapOneInAfterConvertEvent(Mono<T> result, String collectionName) {
        return result.flatMap(loaded -> {
            return reactiveEventPublisher.publishEvent(new ReactiveAfterConvertEvent<>(loaded))
                    .thenReturn(loaded);
        });
    }

    @Override
    protected <T> Mono<T> doFindAndModify(String collectionName, Document query, Document fields, Document sort,
                                          Class<T> entityClass, Update update, FindAndModifyOptions options) {
        Mono<T> result = super.doFindAndModify(collectionName, query, fields, sort, entityClass, update, options);
        return wrapOneInAfterConvertEvent(result, collectionName);
    }

    private interface SaveOne<T> {
        Mono<T> save();
    }

    private interface SaveMany<T> {
        Flux<T> save();
    }

    private static class BeforeConvertEventFromReactiveContext<T> extends BeforeConvertEvent<T>
            implements ReactiveOrigin {
        @SuppressWarnings("ConstantConditions")
        BeforeConvertEventFromReactiveContext(BeforeConvertEvent<T> event) {
            super(event.getSource(), event.getCollectionName());
        }
    }

    private static class AfterSaveEventFromReactiveContext<T> extends AfterSaveEvent<T> implements ReactiveOrigin {
        @SuppressWarnings("ConstantConditions")
        AfterSaveEventFromReactiveContext(AfterSaveEvent<T> event) {
            super(event.getSource(), event.getDocument(), event.getCollectionName());
        }
    }

    private static class AfterConvertEventFromReactiveContext<T> extends AfterConvertEvent<T>
            implements ReactiveOrigin {
        @SuppressWarnings("ConstantConditions")
        AfterConvertEventFromReactiveContext(AfterConvertEvent<T> event) {
            super(event.getDocument(), event.getSource(), event.getCollectionName());
        }
    }
}
