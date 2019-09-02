package io.extremum.everything.collection;

import io.extremum.common.model.Model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author rpuch
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({java.lang.annotation.ElementType.FIELD, ElementType.METHOD})
public @interface CollectionElementType {
    Class<? extends Model> value();
}
