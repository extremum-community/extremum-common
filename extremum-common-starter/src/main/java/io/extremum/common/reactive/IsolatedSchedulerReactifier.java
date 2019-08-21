package io.extremum.common.reactive;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.function.Supplier;

/**
 * A {@link Reactifier} implementation that uses an isolated {@link reactor.core.scheduler.Scheduler}
 * (that in turn uses an isolated thread pool). This, in turn, allows to isolate pseudo-reactiveness
 * from the true one.
 */
@RequiredArgsConstructor
public class IsolatedSchedulerReactifier implements Reactifier {
    private final Scheduler scheduler;

    @Override
    public <T> Mono<T> mono(Supplier<T> objectSupplier) {
        return Mono.defer(() -> Mono.just(objectSupplier.get()))
                .subscribeOn(scheduler);
    }
}
