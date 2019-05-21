package com.extremum.common.utils;

import com.extremum.common.models.Model;
import com.extremum.common.models.annotation.ModelName;

import java.util.Arrays;

public class ModelUtils {
    private static final String[] checkList = {"HibernateProxy"};

    @SuppressWarnings("unchecked")
    public static String getModelName(Class<? extends Model> modelClass) {
        final String name = modelClass.getName();
        if (Arrays.stream(checkList).anyMatch(name::contains)) {
//            safe because superclass is original class
            modelClass = (Class<? extends Model>) modelClass.getSuperclass();
        }

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
