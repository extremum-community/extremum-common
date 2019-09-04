package io.extremum.common.reactive;

import org.springframework.context.ApplicationEvent;
import reactor.core.publisher.Mono;

import java.util.EventListener;

public interface ReactiveApplicationListener<E extends ApplicationEvent> extends EventListener {

     /**
     * Handle an application event.
     * @param event the event to respond to
     */
    Mono<Void> onApplicationEvent(E event);

}
