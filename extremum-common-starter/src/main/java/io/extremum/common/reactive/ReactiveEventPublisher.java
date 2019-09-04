package io.extremum.common.reactive;

import org.springframework.context.ApplicationEvent;
import reactor.core.publisher.Mono;

public interface ReactiveEventPublisher {
    Mono<Void> publishEvent(ApplicationEvent event);
}
