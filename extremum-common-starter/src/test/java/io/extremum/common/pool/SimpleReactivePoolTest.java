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

        pool = new SimpleReactivePool<>(3, 0.1f, 1000, 1, stringAllocator);

        assertThat(pool.get().block(), is("one"));
        assertThat(pool.get().block(), is("two"));
        assertThat(pool.get().block(), is("three"));
        assertThat(pool.get().block(), is("one"));
    }

    @Test
    void shouldNotAllocateIfNothingIsRequestedYet() throws InterruptedException {
        pool = new SimpleReactivePool<>(3, 0.1f, 1000, 1, stringAllocator);

        waitToLetPoolMakeAnAllocation();

        verify(stringAllocator, never()).allocate(anyInt());
    }

    private void waitToLetPoolMakeAnAllocation() throws InterruptedException {
        Thread.sleep(100);
    }
}