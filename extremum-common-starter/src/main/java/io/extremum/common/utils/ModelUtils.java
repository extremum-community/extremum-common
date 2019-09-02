package io.extremum.common.utils;

import io.extremum.common.model.Model;
import io.extremum.common.model.annotation.HardDelete;
import io.extremum.common.model.annotation.ModelName;

public final class ModelUtils {
    public static String getModelName(Class<? extends Model> modelClass) {
        ModelName annotation = findModelNameAnnotation(modelClass);
        if (annotation == null) {
            throw new IllegalStateException(modelClass + " is not annotated with @ModelName");
        }
        return annotation.value();
    }

    private static ModelName findModelNameAnnotation(Class<? extends Model> modelClass) {
        return AnnotationUtils.findAnnotationDirectlyOrUnderProxy(ModelName.class, modelClass);
    }

    public static boolean hasModelName(Class<? extends Model> modelClass) {
        return findModelNameAnnotation(modelClass) != null;
    }

    public static <M extends Model> String getModelName(M model) {
        return getModelName(model.getClass());
    }

    public static boolean isSoftDeletable(Class<?> modelClass) {
        HardDelete hardDelete = AnnotationUtils.findAnnotationDirectlyOrUnderProxy(HardDelete.class, modelClass);
        return hardDelete == null;
    }

    private ModelUtils() {
    }
}
