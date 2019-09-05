package io.extremum.mongo.springdata;

/**
 * @author rpuch
 */
public class ReactiveAfterConvertEvent<T> extends ReactiveMongoMappingEvent<T> {

    public ReactiveAfterConvertEvent(T source) {
        super(source);
    }
}
