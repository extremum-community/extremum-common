package io.extremum.common.dao;

import java.util.List;
import java.util.Optional;

public interface CommonDao<M, ID> {
    Optional<M> findById(ID id);

    boolean existsById(ID id);

    <N extends M> N save(N model);

    <N extends M> List<N> saveAll(Iterable<N> entities);

    void deleteById(ID id);

    M deleteByIdAndReturn(ID id);
}
