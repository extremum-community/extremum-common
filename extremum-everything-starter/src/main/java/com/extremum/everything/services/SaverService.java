package com.extremum.everything.services;

import com.extremum.common.models.Model;

/**
 * Service that is used save a model to the database for Everything-Everything PATCH operation.
 *
 * @param <M> model type
 */
public interface SaverService<M extends Model> extends EverythingEverythingService {
    /**
     * Saves a given model instance to the storage.
     *
     * @param model model to save
     * @return saved model (can be quite different from the object passed as an argument)
     */
    M save(M model);
}
