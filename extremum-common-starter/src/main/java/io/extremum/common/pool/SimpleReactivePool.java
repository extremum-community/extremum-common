package io.extremum.common.pool;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.*;

public class SimpleReactivePool<T> implements ReactivePool<T> {
    private final int batchSize;
    private final float startAllocationThreshold;
    private final Allocator<T> allocator;
    private final Scheduler schedulerToWaitForAllocation;

    private final BlockingQueue<T> elements;
    private final RunOnFlagOrPeriodically allocation;

    public SimpleReactivePool(int batchSize, float startAllocationThreshold, int maxClientsToWaitForAllocation,
                              Allocator<T> allocator) {
        this.batchSize = batchSize;
        this.startAllocationThreshold = startAllocationThreshold;
        this.allocator = allocator;

        elements = new ArrayBlockingQueue<>(batchSize * 2);

        ExecutorService executorService = newBoundedSingleThreadExecutor(maxClientsToWaitForAllocation);
        schedulerToWaitForAllocation = Schedulers.fromExecutorService(executorService);

        allocation = new RunOnFlagOrPeriodically(new AllocateConditionally());
    }

    private ThreadPoolExecutor newBoundedSingleThreadExecutor(int maxClientsToWaitForAllocation) {
        return new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(maxClientsToWaitForAllocation));
    }

    @Override
    public Mono<T> get() {
        return Mono.defer(() -> {
            T value = elements.poll();
            if (value != null) {
                return Mono.just(value);
            }

            return Mono.fromCallable(() -> {
                requestAllocationIfTooFewLeft();
                return elements.take();
            }).subscribeOn(schedulerToWaitForAllocation);
        });
    }

    private void requestAllocationIfTooFewLeft() {
        if (tooFewLeft()) {
            requestAllocation();
        }
    }

    private boolean tooFewLeft() {
        return (float) elements.size() / batchSize < startAllocationThreshold;
    }

    private void requestAllocation() {
        allocation.raiseFlag();
    }

    @PreDestroy
    public void shutdown() {
        allocation.shutdown();
        schedulerToWaitForAllocation.dispose();
    }

    private class AllocateConditionally implements Runnable {
        @Override
        public void run() {
            if (tooFewLeft()) {
                allocate();
            }
        }

        private void allocate() {
            List<T> newElements = allocator.allocate(batchSize);
            elements.addAll(newElements);
        }
    }
}
