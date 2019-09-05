package io.extremum.mongo.springdata;

import io.extremum.common.reactive.ReactiveEventPublisher;
import io.extremum.mongo.springdata.lifecycle.ReactiveOrigin;
import org.bson.Document;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.convert.MongoWriter;
import org.springframework.data.mongodb.core.mapping.event.AfterConvertEvent;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.data.mongodb.core.mapping.event.MongoMappingEvent;
import org.springframework.data.mongodb.core.query.Collation;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

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
            event = (E) new ReactiveBeforeConvertEvent<>((BeforeConvertEvent<T>) event);
        }
        if (event instanceof AfterSaveEvent) {
            //noinspection unchecked
            event = (E) new ReactiveAfterSaveEvent<>((AfterSaveEvent<T>) event);
        }
        if (event instanceof AfterConvertEvent) {
            //noinspection unchecked
            event = (E) new ReactiveAfterConvertEvent<>((AfterConvertEvent<T>) event);
        }
        return event;
    }

    @Override
    protected <T> Mono<T> doInsert(String collectionName, T objectToSave, MongoWriter<Object> writer) {
        return wrapOneInBeforeConvertAndAfterSaveEvents(objectToSave, collectionName,
                () -> super.doInsert(collectionName, objectToSave, writer));
    }

    private <T> Mono<T> wrapOneInBeforeConvertAndAfterSaveEvents(T objectToSave, String collectionName,
                                                                 SaveOne<T> saver) {
        return reactiveEventPublisher.publishEvent(new BeforeConvertEvent<>(objectToSave, collectionName))
                .then(saver.save())
                .flatMap(saved -> {
                    return reactiveEventPublisher.publishEvent(new AfterSaveEvent<>(saved, null, collectionName))
                            .thenReturn(saved);
                });
    }

    @Override
    protected <T> Flux<T> doInsertBatch(String collectionName, Collection<? extends T> batchToSave,
                                        MongoWriter<Object> writer) {
        return wrapManyInBeforeConvertAndAfterSaveEvents(batchToSave, collectionName,
                () -> super.doInsertBatch(collectionName, batchToSave, writer));
    }

    private <T> Flux<T> wrapManyInBeforeConvertAndAfterSaveEvents(Iterable<? extends T> batchToSave,
                                                                  String collectionName, SaveMany<T> saver) {
        return Flux.fromIterable(batchToSave)
                .map(objectToSave -> reactiveEventPublisher.publishEvent(
                        new BeforeConvertEvent<>(objectToSave, collectionName)))
                .thenMany(saver.save())
                .flatMap(saved -> {
                    return reactiveEventPublisher.publishEvent(new AfterSaveEvent<>(saved, null, collectionName))
                            .thenReturn(saved);
                });
    }

    @Override
    public <T> Flux<T> find(Query query, Class<T> entityClass, String collectionName) {
        return super.find(query, entityClass, collectionName)
                .flatMap(loaded -> {
                    return reactiveEventPublisher.publishEvent(new AfterConvertEvent<>(null, loaded, collectionName))
                            .thenReturn(loaded);
                });
    }

    @Override
    protected <T> Mono<T> doFindOne(String collectionName, Document query, Document fields, Class<T> entityClass,
                                    Collation collation) {
        Mono<T> result = super.doFindOne(collectionName, query, fields, entityClass, collation);
        return wrapOneInAfterConvertEvent(result, collectionName);
    }

    private <T> Mono<T> wrapOneInAfterConvertEvent(Mono<T> result, String collectionName) {
        return result.flatMap(loaded -> {
            return reactiveEventPublisher.publishEvent(new AfterConvertEvent<>(null, loaded, collectionName))
                    .thenReturn(loaded);
        });
    }

    @Override
    protected <T> Mono<T> doFindAndModify(String collectionName, Document query, Document fields, Document sort, Class<T> entityClass, Update update, FindAndModifyOptions options) {
        Mono<T> result = super.doFindAndModify(collectionName, query, fields, sort, entityClass, update, options);
        return wrapOneInAfterConvertEvent(result, collectionName);
    }

    private interface SaveOne<T> {
        Mono<T> save();
    }

    private interface SaveMany<T> {
        Flux<T> save();
    }

    private static class ReactiveBeforeConvertEvent<T> extends BeforeConvertEvent<T> implements ReactiveOrigin {
        @SuppressWarnings("ConstantConditions")
        ReactiveBeforeConvertEvent(BeforeConvertEvent<T> event) {
            super(event.getSource(), event.getCollectionName());
        }
    }

    private static class ReactiveAfterSaveEvent<T> extends AfterSaveEvent<T> implements ReactiveOrigin {
        @SuppressWarnings("ConstantConditions")
        ReactiveAfterSaveEvent(AfterSaveEvent<T> event) {
            super(event.getSource(), event.getDocument(), event.getCollectionName());
        }
    }

    private static class ReactiveAfterConvertEvent<T> extends AfterConvertEvent<T> implements ReactiveOrigin {
        @SuppressWarnings("ConstantConditions")
        ReactiveAfterConvertEvent(AfterConvertEvent<T> event) {
            super(event.getDocument(), event.getSource(), event.getCollectionName());
        }
    }
}
