package io.extremum.common.reactive;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

class IsolatedSchedulerReactifierTest {
    private IsolatedSchedulerReactifier reactifier;

    @Test
    void yieldsCorrectResult() {
        reactifier = new IsolatedSchedulerReactifier(Schedulers.elastic());

        Mono<Integer> mono = reactifier.mono(() -> 42);

        assertThat(mono.block(), is(42));
    }

    @Test
    void executesOnTheSpecifiedScheduler() throws InterruptedException {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Thread singleExecutorThread = getExecutorThread(executorService);

        reactifier = new IsolatedSchedulerReactifier(Schedulers.fromExecutorService(executorService));
        AtomicReference<Thread> executingThreadRef = new AtomicReference<>();

        Mono<Integer> mono = reactifier.mono(() -> {
            executingThreadRef.set(Thread.currentThread());
            return 42;
        });
        mono.block();

        assertThat(executingThreadRef.get(), is(sameInstance(singleExecutorThread)));
    }

    private Thread getExecutorThread(ExecutorService executorService) throws InterruptedException {
        AtomicReference<Thread> threadRef = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        executorService.submit(() -> {
            threadRef.set(Thread.currentThread());
            latch.countDown();
        });

        latch.await(5, TimeUnit.SECONDS);

        assertThat("No thread was recorded in 5 seconds", threadRef.get(), is(notNullValue()));

        return threadRef.get();
    }
}