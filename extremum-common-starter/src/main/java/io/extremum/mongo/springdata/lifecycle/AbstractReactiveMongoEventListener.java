package io.extremum.mongo.springdata.lifecycle;

import io.extremum.common.reactive.ReactiveApplicationListener;
import io.extremum.mongo.springdata.ReactiveAfterConvertEvent;
import io.extremum.mongo.springdata.ReactiveAfterSaveEvent;
import io.extremum.mongo.springdata.ReactiveBeforeConvertEvent;
import io.extremum.mongo.springdata.ReactiveMongoMappingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.GenericTypeResolver;
import reactor.core.publisher.Mono;

public abstract class AbstractReactiveMongoEventListener<E>
        implements ReactiveApplicationListener<ReactiveMongoMappingEvent<?>> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractReactiveMongoEventListener.class);

    private final Class<?> domainClass;

    public AbstractReactiveMongoEventListener() {
        Class<?> typeArgument = GenericTypeResolver.resolveTypeArgument(this.getClass(),
                AbstractReactiveMongoEventListener.class);
        this.domainClass = typeArgument == null ? Object.class : typeArgument;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Mono<Void> onApplicationEvent(ReactiveMongoMappingEvent<?> event) {

        Object source = event.getSource();

        // Check for matching domain type and invoke callbacks
        if (!domainClass.isAssignableFrom(source.getClass())) {
            return Mono.empty();
        }

        if (event instanceof ReactiveBeforeConvertEvent) {
            return onBeforeConvert((ReactiveBeforeConvertEvent<E>) event);
        } else if (event instanceof ReactiveAfterSaveEvent) {
            return onAfterSave((ReactiveAfterSaveEvent<E>) event);
        } else if (event instanceof ReactiveAfterConvertEvent) {
            return onAfterConvert((ReactiveAfterConvertEvent<E>) event);
        }

        return Mono.empty();
    }

    /**
     * Captures {@link ReactiveBeforeConvertEvent}.
     *
     * @param event never {@literal null}.
     */
    public Mono<Void> onBeforeConvert(ReactiveBeforeConvertEvent<E> event) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("onBeforeConvert({})", event.getSource());
        }

        return Mono.empty();
    }

    /**
     * Captures {@link ReactiveAfterSaveEvent}.
     *
     * @param event will never be {@literal null}.
     */
    public Mono<Void> onAfterSave(ReactiveAfterSaveEvent<E> event) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("onAfterSave({})", event.getSource());
        }

        return Mono.empty();
    }

    /**
     * Captures {@link ReactiveAfterConvertEvent}.
     *
     * @param event will never be {@literal null}.
     */
    public Mono<Void> onAfterConvert(ReactiveAfterConvertEvent<E> event) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("onAfterConvert({})", event.getSource());
        }

        return Mono.empty();
    }
}