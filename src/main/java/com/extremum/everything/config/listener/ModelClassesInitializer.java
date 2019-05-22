package com.extremum.everything.config.listener;

import com.extremum.common.models.Model;
import com.extremum.common.models.annotation.ModelName;
import com.extremum.common.utils.FindUtils;
import com.extremum.common.utils.ModelUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ModelClassesInitializer implements ApplicationListener<ContextRefreshedEvent> {
    private final List<String> modelPackages;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        List<Class<? extends Model>> models = new ArrayList<>();
        for (String packageName : modelPackages) {
            models.addAll(FindUtils.findClassesByAnnotation(Model.class, ModelName.class, packageName));
        }

        ModelClasses.setModelNameToClassMap(
                models.stream()
                        .collect(Collectors.toMap(
                                ModelUtils::getModelName,
                                aClass -> aClass,
                                (aClass, aClass2) -> {
                                    throw new IllegalStateException("Founded model with duplicate ModelName value: " + ModelUtils.getModelName(aClass));
                                })));

    }
}
