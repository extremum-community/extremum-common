package io.extremum.common.service;

import io.extremum.sharedmodels.basic.Model;

import java.util.List;

public interface CommonService<M extends Model> {
    M get(String id);

    M get(String id, Problems problems);

    M delete(String id);

    M delete(String id, Problems alerts);

    M save(M data);

    M save(M data, Problems problems);

    M create(M data);

    M create(M data, Problems problems);

    List<M> create(List<M> data);

    List<M> create(List<M> data, Problems problems);
}
