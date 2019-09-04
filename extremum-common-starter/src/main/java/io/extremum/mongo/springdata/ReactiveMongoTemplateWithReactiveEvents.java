package io.extremum.mongo.springdata;

import io.extremum.common.reactive.ReactiveEventPublisher;
import io.extremum.mongo.springdata.lifecycle.ReactiveOrigin;
import org.bson.Document;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.mapping.event.*;
import org.springframework.data.mongodb.core.query.Collation;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
    public <T> Mono<T> insert(T objectToSave, String collectionName) {
        return reactiveEventPublisher.publishEvent(new BeforeConvertEvent<>(objectToSave, collectionName))
                .then(super.insert(objectToSave, collectionName))
                .flatMap(inserted -> {
                    return reactiveEventPublisher.publishEvent(new AfterSaveEvent<>(inserted, null, collectionName))
                            .thenReturn(inserted);
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
        return super.doFindOne(collectionName, query, fields, entityClass, collation)
                .flatMap(loaded -> {
                    return reactiveEventPublisher.publishEvent(new AfterConvertEvent<>(null, loaded, collectionName))
                            .thenReturn(loaded);
                });
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
