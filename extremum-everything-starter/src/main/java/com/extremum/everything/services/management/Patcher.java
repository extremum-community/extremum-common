package com.extremum.everything.services.management;

import com.extremum.common.models.Model;
import com.extremum.sharedmodels.descriptor.Descriptor;
import com.github.fge.jsonpatch.JsonPatch;

/**
 * @author rpuch
 */
public interface Patcher {
    Model patch(Descriptor id, Model modelToPatch, JsonPatch patch);
}
