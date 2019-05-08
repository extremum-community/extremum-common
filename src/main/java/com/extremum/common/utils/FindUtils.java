package com.extremum.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class FindUtils {
    private static ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);

    private static void resetProvider() {
        provider.resetFilters(false);
    }

    @SuppressWarnings("unchecked")
    private static <M> List<Class<? extends M>> findCandidates(String scanPackage, Class<M> resultClass) {
        List<Class<? extends M>> resultList = new ArrayList<>();
        for (BeanDefinition definition : provider.findCandidateComponents(scanPackage)) {
            try {
                resultList.add((Class<? extends M>) Class.forName(definition.getBeanClassName()));
                log.info("Class found : {}", definition.getBeanClassName());
            } catch (ClassNotFoundException e) {
                log.error("Class not found for name: {}", definition.getBeanClassName());
            }
        }
        return resultList;
    }

    public static <M> List<Class<? extends M>> findClassesByInterface(Class<M> interfaceClass, String scanPackage) {
        resetProvider();
        provider.addIncludeFilter(new AssignableTypeFilter(interfaceClass));
        return findCandidates(scanPackage, interfaceClass);
    }

    public static <M> List<Class<? extends M>> findClassesByAnnotation(Class<M> resultSuperClass, Class<? extends Annotation> annotationClass, String scanPackage) {
        resetProvider();
        provider.addIncludeFilter(new AnnotationTypeFilter(annotationClass));
        return findCandidates(scanPackage, resultSuperClass);
    }
}
