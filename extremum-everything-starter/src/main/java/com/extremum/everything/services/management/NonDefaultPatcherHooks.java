package com.extremum.everything.services.management;

import com.extremum.common.models.Model;
import com.extremum.everything.services.PatchPersistenceContext;
import com.extremum.everything.services.PatcherHooksService;
import com.extremum.sharedmodels.dto.RequestDto;

/**
 * @author rpuch
 */
final class NonDefaultPatcherHooks<M extends Model, D extends RequestDto> implements PatcherHooks<M, D> {
    private final PatcherHooksService<M, D> patcherHooksService;

    NonDefaultPatcherHooks(PatcherHooksService<M, D> patcherHooksService) {
        this.patcherHooksService = patcherHooksService;
    }

    @Override
    public D afterPatchAppliedToDto(D dto) {
        return patcherHooksService.afterPatchAppliedToDto(dto);
    }

    @Override
    public void beforeSave(PatchPersistenceContext<M> context) {
        patcherHooksService.beforeSave(context);
    }

    @Override
    public void afterSave(PatchPersistenceContext<M> context) {
        patcherHooksService.afterSave(context);
    }
}
