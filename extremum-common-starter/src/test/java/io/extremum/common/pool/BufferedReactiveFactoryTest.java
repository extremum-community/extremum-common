package io.extremum.common.pool;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BufferedReactiveFactoryTest {
    @Mock
    private Allocator<String> stringAllocator;

    private BufferedReactiveFactory<String> pool;

    @AfterEach
    void shutdownPool() {
        if (pool != null) {
            pool.shutdown();
        }
    }

    @Test
    void shouldReturnWhatAllocatorReturns() {
        when(stringAllocator.allocate(3))
                .thenReturn(Arrays.asList("one", "two", "three"));

        pool = buildPool();

        assertThat(pool.get().block(), is("one"));
        assertThat(pool.get().block(), is("two"));
        assertThat(pool.get().block(), is("three"));
        assertThat(pool.get().block(), is("one"));
    }

    private BufferedReactiveFactory<String> buildPool() {
        BufferedReactiveFactoryConfig config = BufferedReactiveFactoryConfig.builder()
                .batchSize(3)
                .startAllocationThreshold(0.1f)
                .maxClientsToWaitForAllocation(1000)
                .checkForAllocationEachMillis(1)
                .build();
        return new BufferedReactiveFactory<>(config, stringAllocator);
    }

    @Test
    void shouldNotAllocateIfNothingIsRequestedYet() throws InterruptedException {
        pool = buildPool();

        waitToLetPoolMakeAnAllocation();

        verify(stringAllocator, never()).allocate(anyInt());
    }

    private void waitToLetPoolMakeAnAllocation() throws InterruptedException {
        Thread.sleep(100);
    }

    @Test
    void shouldRejectRequestsWhenWaitingCapacityIsExhausted() {
        /*
         * Here, 3 clients are making requests at the same time. One gets executed,
         * another one is put in executor queue (allowed by maxClientsToWaitForAllocation(1),
         * the third one fails with RejectedExecutionException.
         */

        pool = new BufferedReactiveFactory<>(configWithMax1ClientAllowedToWait(), slowAllocator());

        AtomicInteger successCounter = new AtomicInteger(0);
        List<Throwable> exceptions = new CopyOnWriteArrayList<>();

        Runnable task = () -> pool.get()
                .doOnNext(x -> successCounter.incrementAndGet())
                .doOnError(exceptions::add)
                .block();
        List<Thread> threads = IntStream.range(0, 3)
                .mapToObj(i -> new Thread(task))
                .collect(Collectors.toList());;

        threads.forEach(Thread::start);
        threads.forEach(this::joinThread);

        assertThatTwoTasksExecutedSuccessfully(successCounter);
        assertThatThereIsOneRejectedExecutionException(exceptions);
    }

    private BufferedReactiveFactoryConfig configWithMax1ClientAllowedToWait() {
        return BufferedReactiveFactoryConfig.builder()
                    .batchSize(100)
                    .maxClientsToWaitForAllocation(1)
                    .build();
    }

    @NotNull
    private Allocator<String> slowAllocator() {
        return quantityToAllocate -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return IntStream.range(0, quantityToAllocate)
                    .mapToObj(Integer::toString)
                    .collect(Collectors.toList());
        };
    }

    private void joinThread(Thread t) {
        try {
            t.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void assertThatTwoTasksExecutedSuccessfully(AtomicInteger successCounter) {
        assertThat(successCounter.get(), is(2));
    }

    private void assertThatThereIsOneRejectedExecutionException(List<Throwable> exceptions) {
        assertThat(exceptions, hasSize(1));
        assertThat(exceptions.get(0), is(instanceOf(RejectedExecutionException.class)));
    }
}