package com.extremum.everything.support;

import com.extremum.common.models.Model;

/**
 * @author rpuch
 */
public interface ModelDescriptors {
    <M extends Model> Class<M> getModelClassByDescriptorId(String internalId);
}
