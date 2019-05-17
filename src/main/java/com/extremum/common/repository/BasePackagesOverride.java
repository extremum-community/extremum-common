package com.extremum.common.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.util.MultiValueMap;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author rpuch
 */
@RequiredArgsConstructor
public class BasePackagesOverride implements AnnotationMetadata {
    private final AnnotationMetadata annotationMetadata;
    private final Class<? extends Annotation> annotationClass;
    private final String repositoryPackagesPropertyKey;
    private final ApplicationProperties applicationProperties = new ApplicationProperties();

    @Override
    public Map<String, Object> getAnnotationAttributes(String annotationName) {
        Map<String, Object> attributes = annotationMetadata.getAnnotationAttributes(annotationName);
        if (attributes != null && annotationClass.getName().equals(annotationName)) {
            List<String> packageNamesFromConfig = applicationProperties.getTrimmedStringList(
                    repositoryPackagesPropertyKey);
            attributes.put("basePackages", packageNamesFromConfig.toArray(new String[0]));
        }
        return attributes;
    }

    // All the other methods just delegate

    @Override
    public Set<String> getAnnotationTypes() {
        return annotationMetadata.getAnnotationTypes();
    }

    @Override
    public Set<String> getMetaAnnotationTypes(String annotationName) {
        return annotationMetadata.getMetaAnnotationTypes(annotationName);
    }

    @Override
    public boolean hasAnnotation(String annotationName) {
        return annotationMetadata.hasAnnotation(annotationName);
    }

    @Override
    public boolean hasMetaAnnotation(String metaAnnotationName) {
        return annotationMetadata.hasMetaAnnotation(metaAnnotationName);
    }

    @Override
    public boolean hasAnnotatedMethods(String annotationName) {
        return annotationMetadata.hasAnnotatedMethods(annotationName);
    }

    @Override
    public Set<MethodMetadata> getAnnotatedMethods(String annotationName) {
        return annotationMetadata.getAnnotatedMethods(annotationName);
    }

    @Override
    public boolean isAnnotated(String annotationName) {
        return annotationMetadata.isAnnotated(annotationName);
    }

    @Override
    public Map<String, Object> getAnnotationAttributes(String annotationName, boolean classValuesAsString) {
        return annotationMetadata.getAnnotationAttributes(annotationName, classValuesAsString);
    }

    @Override
    public MultiValueMap<String, Object> getAllAnnotationAttributes(String annotationName) {
        return annotationMetadata.getAllAnnotationAttributes(annotationName);
    }

    @Override
    public MultiValueMap<String, Object> getAllAnnotationAttributes(String annotationName,
            boolean classValuesAsString) {
        return annotationMetadata.getAllAnnotationAttributes(annotationName, classValuesAsString);
    }

    @Override
    public String getClassName() {
        return annotationMetadata.getClassName();
    }

    @Override
    public boolean isInterface() {
        return annotationMetadata.isInterface();
    }

    @Override
    public boolean isAnnotation() {
        return annotationMetadata.isAnnotation();
    }

    @Override
    public boolean isAbstract() {
        return annotationMetadata.isAbstract();
    }

    @Override
    public boolean isConcrete() {
        return annotationMetadata.isConcrete();
    }

    @Override
    public boolean isFinal() {
        return annotationMetadata.isFinal();
    }

    @Override
    public boolean isIndependent() {
        return annotationMetadata.isIndependent();
    }

    @Override
    public boolean hasEnclosingClass() {
        return annotationMetadata.hasEnclosingClass();
    }

    @Override
    public String getEnclosingClassName() {
        return annotationMetadata.getEnclosingClassName();
    }

    @Override
    public boolean hasSuperClass() {
        return annotationMetadata.hasSuperClass();
    }

    @Override
    public String getSuperClassName() {
        return annotationMetadata.getSuperClassName();
    }

    @Override
    public String[] getInterfaceNames() {
        return annotationMetadata.getInterfaceNames();
    }

    @Override
    public String[] getMemberClassNames() {
        return annotationMetadata.getMemberClassNames();
    }
}
