package com.extremum.everything.collection;

import com.extremum.common.models.Model;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author rpuch
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(java.lang.annotation.ElementType.FIELD)
public @interface CollectionElementType {
    Class<? extends Model> value();
}
