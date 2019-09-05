package io.extremum.mongo.springdata;

/**
 * @author rpuch
 */
public class ReactiveAfterSaveEvent<T> extends ReactiveMongoMappingEvent<T> {

    public ReactiveAfterSaveEvent(T source) {
        super(source);
    }
}
