package com.extremum.common.utils;

import com.extremum.common.models.Model;
import com.extremum.common.models.annotation.ModelName;

public class ModelUtils {

    public static String getModelName(Class<? extends Model> modelClass) {
        return modelClass.getAnnotation(ModelName.class).value();
    }

    public static <M extends Model> String getModelName(M model) {
        return getModelName(model.getClass());
    }
}
