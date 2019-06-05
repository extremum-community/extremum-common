package com.extremum.common.utils;

import java.lang.annotation.Annotation;

/**
 * @author rpuch
 */
public interface Attribute {
    String name();

    <A extends Annotation> A getAnnotation(Class<A> annotationClass);

    default boolean isAnnotatedWith(Class<? extends Annotation> annotationClass) {
        return getAnnotation(annotationClass) != null;
    }

    Class<?> getType();

    Object value();
}
