package io.extremum.common.pool;

import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.*;

public class SimpleReactivePool<T> implements ReactivePool<T> {
    private final SimpleReactivePoolConfig config;
    private final Allocator<T> allocator;
    private final Scheduler schedulerToWaitForAllocation;

    private final BlockingQueue<T> elements;
    private final RunOnFlagOrPeriodically allocation;

    public SimpleReactivePool(SimpleReactivePoolConfig config, Allocator<T> allocator) {
        config.validate();

        this.config = config;
        this.allocator = allocator;

        elements = new ArrayBlockingQueue<>(config.getBatchSize() * 2);

        ExecutorService executorService = newBoundedSingleThreadExecutor(config.getMaxClientsToWaitForAllocation());
        schedulerToWaitForAllocation = Schedulers.fromExecutorService(executorService);

        allocation = new RunOnFlagOrPeriodically(config.getCheckForAllocationEachMillis(), new AllocateConditionally());
    }

    private ThreadPoolExecutor newBoundedSingleThreadExecutor(int maxClientsToWaitForAllocation) {
        return new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(maxClientsToWaitForAllocation),
                new CustomizableThreadFactory("wait-for-allocation-"));
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
        return (float) elements.size() / config.getBatchSize() < config.getStartAllocationThreshold();
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
            List<T> newElements = allocator.allocate(config.getBatchSize());
            elements.addAll(newElements);
        }
    }
}
