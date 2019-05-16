package com.extremum.common.utils;

import com.extremum.common.models.Model;
import com.extremum.common.models.annotation.ModelName;

public class ModelUtils {

    public static String getModelName(Class<? extends Model> modelClass) {
        ModelName annotation = modelClass.getAnnotation(ModelName.class);
        if (annotation == null) {
            throw new IllegalStateException(modelClass + " is not annotated with @ModelName");
        }
        return annotation.value();
    }

    public static <M extends Model> String getModelName(M model) {
        return getModelName(model.getClass());
    }
}
