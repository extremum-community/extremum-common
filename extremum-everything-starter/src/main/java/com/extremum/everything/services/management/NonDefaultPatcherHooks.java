package com.extremum.everything.services.management;

import com.extremum.common.models.Model;
import com.extremum.everything.services.PatchPersistenceContext;
import com.extremum.everything.services.PatcherHooksService;
import com.extremum.sharedmodels.dto.RequestDto;

/**
 * @author rpuch
 */
final class NonDefaultPatcherHooks implements PatcherHooks {
    private final PatcherHooksService<Model, RequestDto> patcherHooksService;

    NonDefaultPatcherHooks(PatcherHooksService<Model, RequestDto> patcherHooksService) {
        this.patcherHooksService = patcherHooksService;
    }

    @Override
    public RequestDto afterPatchAppliedToDto(RequestDto patchedDto) {
        return patcherHooksService.afterPatchAppliedToDto(patchedDto);
    }

    @Override
    public void beforeSave(PatchPersistenceContext<Model> context) {
        patcherHooksService.beforeSave(context);
    }

    @Override
    public void afterSave(PatchPersistenceContext<Model> context) {
        patcherHooksService.afterSave(context);
    }
}
