package com.extremum.starter.listener;

import com.extremum.common.models.Model;
import com.extremum.common.models.annotation.ModelName;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class ModelNameToClassInitializer implements ApplicationListener<ContextRefreshedEvent> {
    private final List<String> modelPackages;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        List<Class<? extends Model>> models = new ArrayList<>();
        for (String packageName : modelPackages) {
            models.addAll(findModels(packageName));
        }

        ModelNameToClass.setModelNameToClassMap(
                models.stream()
                        .parallel()
                        .collect(Collectors.toMap(
                                modelClass -> modelClass.getAnnotation(ModelName.class).name(),
                                aClass -> aClass,
                                (aClass, aClass2) -> {
                                    throw new IllegalStateException("Founded model with duplicate ModelName value: " + aClass.getAnnotation(ModelName.class).name());
                                })));
    }

    @SuppressWarnings("unchecked")
    private static List<Class<? extends Model>> findModels(String scanPackage) {
        List<Class<? extends Model>> resultList = new ArrayList<>();
        ClassPathScanningCandidateComponentProvider provider = createComponentScanner();
        for (BeanDefinition definition : provider.findCandidateComponents(scanPackage)) {
            try {
                resultList.add((Class<? extends Model>) Class.forName(definition.getBeanClassName()));
                log.info("Class found : {}", definition.getBeanClassName());
            } catch (ClassNotFoundException e) {
                log.error("Class not found for name: {}", definition.getBeanClassName());
            }
        }
        return resultList;
    }

    private static ClassPathScanningCandidateComponentProvider createComponentScanner() {
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AnnotationTypeFilter(ModelName.class));
        return provider;
    }
}
