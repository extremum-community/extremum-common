package io.extremum.common.support;

import io.extremum.common.models.Model;
import io.extremum.sharedmodels.descriptor.Descriptor;

import java.util.List;

/**
 * @author rpuch
 */
public interface UniversalModelFinder {
    List<Model> findModels(List<Descriptor> ids);
}
