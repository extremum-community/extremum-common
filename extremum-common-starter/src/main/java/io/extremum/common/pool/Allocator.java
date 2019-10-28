package io.extremum.common.pool;

import java.util.List;

public interface Allocator<T> {
    List<T> allocate(int quantityToAllocate);
}
