package com.extremum.everything.services;

import com.extremum.common.models.Model;

public interface GetterService<M extends Model> extends EverythingEverythingService {
    /**
     * Search and returns a found object
     *
     * @param id descriptor internal ID
     * @return found object if its found or null otherwise
     */
    M get(String id);
}
