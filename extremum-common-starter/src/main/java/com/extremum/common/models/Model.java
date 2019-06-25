package com.extremum.common.models;

import java.io.Serializable;

/**
 * Base interface for models
 */
public interface Model extends Serializable {
    default void copyServiceFieldsTo(Model to) {
        // nothing to copy here
    }
}
