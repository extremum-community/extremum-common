package com.extremum.common.service;

import com.extremum.common.models.Model;

import java.util.List;

//TODO close public methods with Problems
public interface CommonService<M extends Model> {
    M get(String id);

    M get(String id, Problems problems);

    void delete(String id);

    /**
     * @param id internalId of model descriptor
     * @apiNote if you change signature of this method, you need to change isDeleteMethod() on WatchInvocationHandler child on watch-starter
     */
    void delete(String id, Problems alerts);

    List<M> list();

    List<M> list(Problems problems);

    M save(M data);

    /**
     * @param data new model to save
     * @apiNote if you change signature of this method, you need to change isSaveMethod() on WatchInvocationHandler child on watch-starter
     */
    M save(M data, Problems problems);

    M create(M data);

    M create(M data, Problems problems);

    List<M> create(List<M> data);

    List<M> create(List<M> data, Problems problems);
}
