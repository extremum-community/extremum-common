package com.extremum.everything.services;

import com.extremum.common.models.Model;
import com.github.fge.jsonpatch.JsonPatch;

public interface PatcherService<M extends Model> extends EverythingEverythingService {
    /**
     * Performs patching an object with passed ID
     *
     * @param id    ID of the patching object
     * @param patch pacth value for patching of object with passed ID
     * @return patched object
     */
    M patch(String id, JsonPatch patch);
}
