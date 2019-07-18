package com.extremum.everything.services.management;

import com.extremum.common.models.Model;
import com.extremum.everything.services.PatchPersistenceContext;
import com.extremum.sharedmodels.dto.RequestDto;

/**
 * @author rpuch
 */
final class NullPatcherHooks<M extends Model, D extends RequestDto> implements PatcherHooks<M, D> {
    @Override
    public D afterPatchAppliedToDto(D dto) {
        return dto;
    }

    @Override
    public void beforeSave(PatchPersistenceContext<M> context) {
        // doing nothing
    }

    @Override
    public void afterSave(PatchPersistenceContext<M> context) {
        // doing nothing
    }
}
