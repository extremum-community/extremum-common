package io.extremum.common.reactive;

import reactor.core.publisher.Mono;

import java.util.function.Supplier;

/**
 * A naive implementation of {@link Reactifier} suitable for tests.
 * DO NOT USE IN PRODUCTION CODE!
 */
public class NaiveReactifier implements Reactifier {
    @Override
    public <T> Mono<T> mono(Supplier<T> objectSupplier) {
        return Mono.defer(() -> Mono.just(objectSupplier.get()));
    }
}
