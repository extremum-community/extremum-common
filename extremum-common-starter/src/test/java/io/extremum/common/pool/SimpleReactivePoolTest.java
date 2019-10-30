package io.extremum.common.pool;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SimpleReactivePoolTest {
    @Mock
    private Allocator<String> stringAllocator;

    private SimpleReactivePool<String> pool;

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

    private SimpleReactivePool<String> buildPool() {
        SimpleReactivePoolConfig config = SimpleReactivePoolConfig.builder()
                .batchSize(3)
                .startAllocationThreshold(0.1f)
                .maxClientsToWaitForAllocation(1000)
                .checkForAllocationEachMillis(1)
                .build();
        return new SimpleReactivePool<>(config, stringAllocator);
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
}