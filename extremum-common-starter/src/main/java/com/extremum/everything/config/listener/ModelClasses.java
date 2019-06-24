package com.extremum.everything.config.listener;

import com.extremum.common.models.Model;

/**
 * @author rpuch
 */
public interface ModelClasses {
    Class<? extends Model> getClassByModelName(String modelName);

    Class<? extends Model> getModelByDescriptorId(String internalId);
}
