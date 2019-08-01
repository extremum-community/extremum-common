package com.extremum.common.support;

import com.extremum.common.models.Model;

/**
 * @author rpuch
 */
public interface ModelClasses {
    <M extends Model> Class<M> getClassByModelName(String modelName);
}
