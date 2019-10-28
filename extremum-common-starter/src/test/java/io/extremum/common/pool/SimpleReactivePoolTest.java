package io.extremum.common.pool;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SimpleReactivePoolTest {
    @Mock
    private Allocator<String> stringAllocator;

    @Test
    void shouldReturnWhatAllocatorReturns() {
        when(stringAllocator.allocate(3))
                .thenReturn(Arrays.asList("one", "two", "three"));

        ReactivePool<String> pool = new SimpleReactivePool<>(3, 0.1f, 1000, stringAllocator);

        assertThat(pool.get().block(), is("one"));
        assertThat(pool.get().block(), is("two"));
        assertThat(pool.get().block(), is("three"));
        assertThat(pool.get().block(), is("one"));
    }
}