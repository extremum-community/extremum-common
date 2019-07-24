package com.extremum.everything.services.management;

import com.extremum.common.models.Model;
import com.extremum.sharedmodels.descriptor.Descriptor;
import com.github.fge.jsonpatch.JsonPatch;

/**
 * @author rpuch
 */
public interface PatchFlow {
    /**
     * @apiNote if you change signature - you need to change isPatchMethod() on WatchInvocationHandler child in extremum-watch-starter
     */
    Model patch(Descriptor id, JsonPatch patch);
}
