package com.extremum.everything.services.management;

import com.extremum.common.models.Model;
import com.extremum.everything.services.PatcherService;
import com.github.fge.jsonpatch.JsonPatch;

/**
 * Uses PatcherService to patch an entity.
 *
 * @author rpuch
 */
class NonDefaultPatcher<M extends Model> implements Patcher<M> {
    private final PatcherService<M> patcherService;

    NonDefaultPatcher(PatcherService<M> patcherService) {
        this.patcherService = patcherService;
    }

    @Override
    public M patch(String id, JsonPatch patch) {
        return patcherService.patch(id, patch);
    }
}
