package io.extremum.common.pool;

import reactor.core.publisher.Mono;

public interface ReactiveObjectFactory<T> {
    Mono<T> get();
}
