package com.extremum.common.dao;

import com.extremum.common.models.Model;

import java.io.Serializable;
import java.util.List;

public interface CommonDao<M extends Model, ID extends Serializable> {
    List<M> findAll();

    M findById(ID id);

    M findById(ID id, String... includeFields);

    boolean isExists(ID id);

    boolean isDeleted(ID id);

    M create(M model);

    M persist(M model);

    boolean remove(ID id);
}
