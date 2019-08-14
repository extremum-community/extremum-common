package io.extremum.common.support;

import io.extremum.common.models.Model;

/**
 * @author rpuch
 */
public interface ModelClasses {
    <M extends Model> Class<M> getClassByModelName(String modelName);
}