package com.extremum.common.service;

import com.extremum.common.models.Model;

import java.util.List;

public interface CommonService<M extends Model> {

    M get(String id);

    M get(String id, Problems problems);

    void delete(String id);

    void delete(String id, Problems alerts);

    List<M> list();

    List<M> list(Problems problems);

    M save(M data);

    M save(M data, Problems problems);

    M create(M data);

    M create(M data, Problems problems);

    List<M> create(List<M> data);

    List<M> create(List<M> data, Problems problems);
}
