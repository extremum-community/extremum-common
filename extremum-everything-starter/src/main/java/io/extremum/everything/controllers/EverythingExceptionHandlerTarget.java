package io.extremum.everything.controllers;

import java.lang.annotation.*;

/**
 * @author rpuch
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface EverythingExceptionHandlerTarget {
}
