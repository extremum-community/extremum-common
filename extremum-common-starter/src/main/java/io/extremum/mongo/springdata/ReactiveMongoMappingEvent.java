package io.extremum.mongo.springdata;

import org.springframework.context.ApplicationEvent;

/**
 * @author rpuch
 */
public abstract class ReactiveMongoMappingEvent<T> extends ApplicationEvent {

    /**
     * Creates new {@link ReactiveMongoMappingEvent}.
     *
     * @param source must not be {@literal null}.
     */
    public ReactiveMongoMappingEvent(T source) {
        super(source);
    }

    /*
     * (non-Javadoc)
     * @see java.util.EventObject#getSource()
     */
    @SuppressWarnings({ "unchecked" })
    @Override
    public T getSource() {
        return (T) super.getSource();
    }
}
