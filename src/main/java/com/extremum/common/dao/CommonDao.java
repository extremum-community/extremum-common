package com.extremum.common.dao;

import java.util.List;
import java.util.Optional;

public interface CommonDao<M, ID> {
    List<M> findAll();

    Optional<M> findById(ID id);

    // TODO: can we implement this method using a spring-data @Repository?
//    M findById(ID id, String... includeFields);

    boolean existsById(ID id);

    // TODO: restore
//    boolean isDeleted(ID id);

//    M create(M model);

    <N extends M> N save(N model);

    <N extends M> Iterable<N> saveAll(Iterable<N> entities);

    boolean softDeleteById(ID id);
}
