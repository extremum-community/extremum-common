package io.extremum.everything.support;

import io.extremum.common.models.Model;

/**
 * @author rpuch
 */
public interface ModelDescriptors {
    <M extends Model> Class<M> getModelClassByDescriptorId(String internalId);
}
