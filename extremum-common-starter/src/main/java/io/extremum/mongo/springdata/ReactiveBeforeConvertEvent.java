package io.extremum.mongo.springdata;

/**
 * @author rpuch
 */
public class ReactiveBeforeConvertEvent<T> extends ReactiveMongoMappingEvent<T> {

    public ReactiveBeforeConvertEvent(T source) {
        super(source);
    }
}
