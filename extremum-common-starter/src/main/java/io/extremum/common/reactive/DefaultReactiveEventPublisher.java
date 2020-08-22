package io.extremum.common.reactive;

import com.google.common.collect.ImmutableList;
import org.springframework.context.ApplicationEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public class DefaultReactiveEventPublisher implements ReactiveEventPublisher {
    private final List<ReactiveApplicationListener<? extends ApplicationEvent>> listeners;

    public DefaultReactiveEventPublisher(List<ReactiveApplicationListener<? extends ApplicationEvent>> listeners) {
        this.listeners = ImmutableList.copyOf(listeners);
    }

    @Override
    public Mono<Void> publishEvent(ApplicationEvent event) {
        return Flux.fromIterable(listeners)
                .map(this::castListener)
                .concatMap(listener -> listener.onApplicationEvent(event))
                .collectList()
                .then();
    }

    @SuppressWarnings("unchecked")
    private ReactiveApplicationListener<ApplicationEvent> castListener(ReactiveApplicationListener<?> listener) {
        return (ReactiveApplicationListener<ApplicationEvent>) listener;
    }
}
