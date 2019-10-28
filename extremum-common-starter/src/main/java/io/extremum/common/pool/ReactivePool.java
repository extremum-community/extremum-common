package io.extremum.common.pool;

import reactor.core.publisher.Mono;

public interface ReactivePool<T> {
    Mono<T> get();
}
