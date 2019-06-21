package com.extremum.everything.services.management;

import com.extremum.common.models.Model;
import com.extremum.everything.services.PatcherService;
import com.github.fge.jsonpatch.JsonPatch;

/**
 * @author rpuch
 */
class PatcherServicePatcher<M extends Model> implements Patcher<M> {
    private final PatcherService<M> patcherService;

    PatcherServicePatcher(PatcherService<M> patcherService) {
        this.patcherService = patcherService;
    }

    @Override
    public M patch(String id, JsonPatch patch) {
        return patcherService.patch(id, patch);
    }
}
