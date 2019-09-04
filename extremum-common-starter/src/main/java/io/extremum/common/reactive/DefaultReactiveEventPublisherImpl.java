package io.extremum.common.reactive;

import com.google.common.collect.ImmutableList;
import org.springframework.context.ApplicationEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public class DefaultReactiveEventPublisherImpl implements ReactiveEventPublisher {
    private final List<ReactiveApplicationListener<?>> listeners;

    public DefaultReactiveEventPublisherImpl(List<ReactiveApplicationListener<?>> listeners) {
        this.listeners = ImmutableList.copyOf(listeners);
    }

    @Override
    public Mono<Void> publishEvent(ApplicationEvent event) {
        return Flux.fromIterable(listeners)
                .map(this::castListener)
                .flatMap(listener -> listener.onApplicationEvent(event))
                .collectList()
                .then();
    }

    @SuppressWarnings("unchecked")
    private ReactiveApplicationListener<ApplicationEvent> castListener(ReactiveApplicationListener<?> listener) {
        return (ReactiveApplicationListener<ApplicationEvent>) listener;
    }
}
