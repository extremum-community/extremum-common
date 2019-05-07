package com.extremum.common.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;

/**
 * Base interface for models
 */
public interface Model extends Serializable {
    @JsonIgnore
    String getModelName();
}
