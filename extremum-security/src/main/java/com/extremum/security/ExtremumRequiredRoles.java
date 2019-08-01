package com.extremum.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author rpuch
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ExtremumRequiredRoles {
    Access defaultAccess() default @Access({});

    Access get() default @Access({});

    Access patch() default @Access({});

    Access remove() default @Access({});
}
