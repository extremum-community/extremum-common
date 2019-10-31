package io.extremum.common.pool;

import reactor.core.publisher.Mono;

public interface ReactiveSupplier<T> {
    Mono<T> get();
}
