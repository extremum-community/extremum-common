package io.extremum.everything.support;

import io.extremum.common.model.Model;

/**
 * @author rpuch
 */
public interface ModelDescriptors {
    <M extends Model> Class<M> getModelClassByModelInternalId(String internalId);
}
