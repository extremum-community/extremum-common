package io.extremum.watch.annotation;

import io.extremum.common.models.Model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for models that implements {@link Model}.
 * Patcher watch works only if model has this annotation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CapturedModel {
}