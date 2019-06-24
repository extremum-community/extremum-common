package com.extremum.everything.support;

import com.extremum.common.models.Model;

/**
 * @author rpuch
 */
public interface ModelClasses {
    Class<? extends Model> getClassByModelName(String modelName);
}
