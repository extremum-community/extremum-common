package io.extremum.common.pool;

import java.util.List;

public interface BatchDestroyer<T> {
    void destroy(List<T> batch);
}
