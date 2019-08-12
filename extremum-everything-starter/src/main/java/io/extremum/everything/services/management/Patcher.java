package io.extremum.everything.services.management;

import io.extremum.common.models.Model;
import io.extremum.sharedmodels.descriptor.Descriptor;
import com.github.fge.jsonpatch.JsonPatch;

/**
 * @author rpuch
 */
public interface Patcher {
    Model patch(Descriptor id, Model modelToPatch, JsonPatch patch);
}
