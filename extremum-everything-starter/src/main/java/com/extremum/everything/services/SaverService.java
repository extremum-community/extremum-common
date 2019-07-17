package com.extremum.everything.services;

import com.extremum.common.models.Model;
import com.github.fge.jsonpatch.JsonPatch;

public interface SaverService<M extends Model> extends EverythingEverythingService {
    /**
     * Saves a given model instance to the storage.
     *
     * @param model model to save
     * @return saved model (can be quite different from the object passed as an argument)
     */
    M save(M model);
}
