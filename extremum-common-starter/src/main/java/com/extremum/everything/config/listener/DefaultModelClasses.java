package com.extremum.everything.config.listener;

import com.extremum.common.models.Model;
import com.extremum.common.models.annotation.ModelName;
import com.extremum.common.utils.FindUtils;
import com.extremum.common.utils.ModelUtils;
import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class DefaultModelClasses implements ModelClasses {
    private final Map<String, Class<? extends Model>> modelNameToClassMap;

    public DefaultModelClasses(List<String> modelPackages) {
        List<Class<? extends Model>> models = new ArrayList<>();
        for (String packageName : modelPackages) {
            models.addAll(FindUtils.findClassesByAnnotation(Model.class, ModelName.class, packageName));
        }

        Map<String, Class<? extends Model>> modelClasses = models.stream()
                .collect(Collectors.toMap(
                        ModelUtils::getModelName,
                        aClass -> aClass,
                        (aClass, aClass2) -> {
                            throw new IllegalStateException(
                                    "Found a model with duplicate ModelName value: " + ModelUtils.getModelName(aClass));
                        }));
        modelNameToClassMap = ImmutableMap.copyOf(modelClasses);
    }

    @Override
    public Class<? extends Model> getClassByModelName(String modelName) {
        return Optional.ofNullable(modelNameToClassMap.get(modelName))
                .orElseThrow(() -> new RuntimeException("Model with name " + modelName
                        + " is not known, probably it doesn't have @ModelName annotation"));
    }

}
