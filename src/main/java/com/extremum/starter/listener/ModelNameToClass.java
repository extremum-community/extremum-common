package com.extremum.starter.listener;

import com.extremum.common.models.Model;

import java.util.Map;
import java.util.Optional;

public class ModelNameToClass {
    private static Map<String, Class<? extends Model>> modelNameToClassMap;

    static void setModelNameToClassMap(Map<String, Class<? extends Model>> modelNameToClassMapIn) {
        modelNameToClassMap = modelNameToClassMapIn;
    }

    public static Optional<Class<? extends Model>> getClassByModelName(String modelName) {
        return Optional.ofNullable(modelNameToClassMap.get(modelName));
    }
}
