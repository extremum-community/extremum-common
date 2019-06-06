package com.extremum.common.utils;

import com.extremum.common.models.Model;
import com.extremum.common.models.annotation.ModelName;
import com.google.common.collect.ImmutableList;

import java.util.List;

public class ModelUtils {
    public static String getModelName(Class<? extends Model> modelClass) {
        ModelName annotation = findModelNameAnnotation(modelClass);
        if (annotation == null) {
            throw new IllegalStateException(modelClass + " is not annotated with @ModelName");
        }
        return annotation.value();
    }

    private static ModelName findModelNameAnnotation(Class<? extends Model> modelClass) {
        final Class<?> classToCheckAnnotation = unwrapProxyClass(modelClass);
        return classToCheckAnnotation.getAnnotation(ModelName.class);
    }

    private static Class<?> unwrapProxyClass(Class<? extends Model> modelClass) {
        if (EntityUtils.isProxyClass(modelClass)) {
            return modelClass.getSuperclass();
        } else {
            return modelClass;
        }
    }

    public static boolean hasModelName(Class<? extends Model> modelClass) {
        return findModelNameAnnotation(modelClass) != null;
    }

    public static <M extends Model> String getModelName(M model) {
        return getModelName(model.getClass());
    }
}
