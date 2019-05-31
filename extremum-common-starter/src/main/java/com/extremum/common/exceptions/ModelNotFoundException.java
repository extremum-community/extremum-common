package com.extremum.common.exceptions;

import com.extremum.common.models.Model;

public class ModelNotFoundException extends CommonException {
    private Class<? extends Model> modelClass;
    private String modelId;

    public ModelNotFoundException(String message) {
        super(message, 404);
    }

    public ModelNotFoundException(Class<? extends Model> modelClass, String modelId) {
        this("Model " + modelClass.getSimpleName() + " with ID " + modelId + " was not found");
        this.modelClass = modelClass;
        this.modelId = modelId;
    }

    public Class<?> getModelClass() {
        return modelClass;
    }

    public String getModelId() {
        return modelId;
    }
}
