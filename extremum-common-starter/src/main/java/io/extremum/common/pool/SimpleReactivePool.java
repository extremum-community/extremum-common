package io.extremum.common.pool;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@RequiredArgsConstructor
public class SimpleReactivePool<T> implements ReactivePool<T> {
    private final Allocator<T> allocator;

    private final Queue<T> queue = new ConcurrentLinkedQueue<>();

    @Override
    public Mono<T> get() {
        return Mono.defer(this::getPossiblyBlockingly);
    }

    private Mono<? extends T> getPossiblyBlockingly() {
        T value = queue.poll();
        if (value != null) {
            return Mono.just(value);
        }

        // FIXME: this is blocking
        allocate();

        return get();
    }

    private synchronized void allocate() {
        if (!queue.isEmpty()) {
            return;
        }

        queue.addAll(allocator.allocate());
    }
}
