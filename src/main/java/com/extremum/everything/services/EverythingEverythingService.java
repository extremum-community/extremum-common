package com.extremum.everything.services;

import com.extremum.common.models.Model;

public interface EverythingEverythingService {
    /**
     * Returns an object name of object supported of service implementation of that interface
     */
    String getSupportedModel();

    default boolean isSupportedModel(Class<? extends Model> modelClass) {
        return false;
    }
}
