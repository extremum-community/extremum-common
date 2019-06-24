package com.extremum.common.service;

import com.extremum.common.models.Model;
import com.extremum.common.response.Alert;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

public interface CommonService<M extends Model> {

    M get(String id);

    M get(String id, Collection<Alert> alerts);

    void delete(String id);

    void delete(String id, Collection<Alert> alerts);

    List<M> list();

    List<M> list(Collection<Alert> alerts);

    M save(M data);

    M save(M data, Collection<Alert> alerts);

    M create(M data);

    M create(M data, Collection<Alert> alerts);

    List<M> create(List<M> data);

    List<M> create(List<M> data, Collection<Alert> alerts);
}
