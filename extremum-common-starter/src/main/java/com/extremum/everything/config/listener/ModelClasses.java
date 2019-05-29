package com.extremum.everything.config.listener;

import com.extremum.common.models.Model;

import java.util.Map;
import java.util.Optional;

public class ModelClasses {
    private static Map<String, Class<? extends Model>> modelNameToClassMap;

    static void setModelNameToClassMap(Map<String, Class<? extends Model>> modelNameToClassMapIn) {
        modelNameToClassMap = modelNameToClassMapIn;
    }

    public static Class<? extends Model> getClassByModelName(String modelName) {
        return Optional.ofNullable(modelNameToClassMap.get(modelName))
                .orElseThrow(() -> new RuntimeException("Model with name " + modelName + "doesn't have annotation ModelName"));
    }
}
