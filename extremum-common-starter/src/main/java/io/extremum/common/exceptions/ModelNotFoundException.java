package io.extremum.common.exceptions;

import io.extremum.sharedmodels.basic.Model;
import org.springframework.http.HttpStatus;

public class ModelNotFoundException extends CommonException {
    private Class<? extends Model> modelClass;
    private String modelId;

    public ModelNotFoundException(String message, Throwable t) {
        super(message, HttpStatus.NOT_FOUND.value(), t);
    }

    public ModelNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND.value());
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
