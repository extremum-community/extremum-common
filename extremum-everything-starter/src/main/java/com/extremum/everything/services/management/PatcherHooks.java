package com.extremum.everything.services.management;

import com.extremum.common.models.Model;
import com.extremum.everything.services.PatchPersistenceContext;
import com.extremum.sharedmodels.dto.RequestDto;

/**
 * @author rpuch
 */
public interface PatcherHooks {
    RequestDto afterPatchAppliedToDto(RequestDto patchedDto);

    void beforeSave(PatchPersistenceContext<Model> context);

    void afterSave(PatchPersistenceContext<Model> context);
}
