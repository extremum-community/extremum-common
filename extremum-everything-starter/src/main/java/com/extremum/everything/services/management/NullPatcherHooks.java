package com.extremum.everything.services.management;

import com.extremum.common.models.Model;
import com.extremum.everything.services.PatchPersistenceContext;
import com.extremum.sharedmodels.dto.RequestDto;

/**
 * @author rpuch
 */
final class NullPatcherHooks implements PatcherHooks {
    @Override
    public RequestDto afterPatchAppliedToDto(RequestDto patchedDto) {
        return patchedDto;
    }

    @Override
    public void beforeSave(PatchPersistenceContext<Model> context) {
        // doing nothing
    }

    @Override
    public void afterSave(PatchPersistenceContext<Model> context) {
        // doing nothing
    }
}
