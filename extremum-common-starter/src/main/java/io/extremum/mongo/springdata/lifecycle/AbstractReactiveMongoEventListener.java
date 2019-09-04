package io.extremum.mongo.springdata.lifecycle;

import io.extremum.common.reactive.ReactiveApplicationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.GenericTypeResolver;
import org.springframework.data.mongodb.core.mapping.event.*;
import reactor.core.publisher.Mono;

public abstract class AbstractReactiveMongoEventListener<E>
        implements ReactiveApplicationListener<MongoMappingEvent<?>> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractReactiveMongoEventListener.class);

    private final Class<?> domainClass;

    public AbstractReactiveMongoEventListener() {
        Class<?> typeArgument = GenericTypeResolver.resolveTypeArgument(this.getClass(),
                AbstractReactiveMongoEventListener.class);
        this.domainClass = typeArgument == null ? Object.class : typeArgument;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Mono<Void> onApplicationEvent(MongoMappingEvent<?> event) {

        if (event instanceof AfterLoadEvent) {
            // we don't support it (yet?)
            return Mono.empty();
        }

        if (event instanceof AbstractDeleteEvent) {
            // we don't support it (yet?)
            return Mono.empty();
        }

        Object source = event.getSource();

        // Check for matching domain type and invoke callbacks
        if (source != null && !domainClass.isAssignableFrom(source.getClass())) {
            return Mono.empty();
        }

        if (event instanceof BeforeConvertEvent) {
            return onBeforeConvert((BeforeConvertEvent<E>) event);
        } else if (event instanceof BeforeSaveEvent) {
            // we don't support it (yet?)
            return Mono.empty();
        } else if (event instanceof AfterSaveEvent) {
            return onAfterSave((AfterSaveEvent<E>) event);
        } else if (event instanceof AfterConvertEvent) {
            return onAfterConvert((AfterConvertEvent<E>) event);
        }

        return Mono.empty();
    }

    /**
     * Captures {@link BeforeConvertEvent}.
     *
     * @param event never {@literal null}.
     */
    public Mono<Void> onBeforeConvert(BeforeConvertEvent<E> event) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("onBeforeConvert({})", event.getSource());
        }

        return Mono.empty();
    }

    /**
     * Captures {@link AfterSaveEvent}.
     *
     * @param event will never be {@literal null}.
     */
    public Mono<Void> onAfterSave(AfterSaveEvent<E> event) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("onAfterSave({}, {})", event.getSource(), event.getDocument());
        }

        return Mono.empty();
    }

    /**
     * Captures {@link AfterConvertEvent}.
     *
     * @param event will never be {@literal null}.
     */
    public Mono<Void> onAfterConvert(AfterConvertEvent<E> event) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("onAfterConvert({}, {})", event.getDocument(), event.getSource());
        }

        return Mono.empty();
    }
}