package com.extremum.common.collection.conversion;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author rpuch
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface MongoEmbeddedCollection {
    String hostFieldName();
}
