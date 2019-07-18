package com.extremum.everything.services.management;

import com.extremum.common.models.Model;
import com.extremum.everything.services.PatchPersistenceContext;
import com.extremum.sharedmodels.dto.RequestDto;

/**
 * @author rpuch
 */
public interface PatcherHooks<M extends Model, D extends RequestDto> {
    D afterPatchAppliedToDto(D dto);

    void beforeSave(PatchPersistenceContext<M> context);

    void afterSave(PatchPersistenceContext<M> context);
}
