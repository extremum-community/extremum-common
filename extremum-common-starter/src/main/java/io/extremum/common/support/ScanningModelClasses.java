package io.extremum.common.support;

import io.extremum.sharedmodels.basic.Model;
import io.extremum.common.model.annotation.ModelName;
import io.extremum.common.utils.FindUtils;
import io.extremum.common.utils.ModelUtils;
import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ScanningModelClasses implements ModelClasses {
    private final Map<String, Class<? extends Model>> modelNameToClassMap;

    public ScanningModelClasses(List<String> modelPackages) {
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
                                    "Found 2 model classes with same model name: " + aClass + " and " + aClass2);
                        }));
        modelNameToClassMap = ImmutableMap.copyOf(modelClasses);
    }

    @Override
    public <M extends Model> Class<M> getClassByModelName(String modelName) {
        @SuppressWarnings("unchecked")
        Class<M> castResult = (Class<M>) Optional.ofNullable(modelNameToClassMap.get(modelName))
                .orElseThrow(() -> new RuntimeException("Model with name " + modelName
                        + " is not known, probably it doesn't have @ModelName annotation"));
        return castResult;
    }

}
