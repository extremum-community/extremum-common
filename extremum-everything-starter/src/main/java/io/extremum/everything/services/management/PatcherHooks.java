package io.extremum.everything.services.management;

import io.extremum.common.models.Model;
import io.extremum.everything.services.PatchPersistenceContext;
import io.extremum.sharedmodels.dto.RequestDto;

/**
 * @author rpuch
 */
public interface PatcherHooks {
    RequestDto afterPatchAppliedToDto(RequestDto patchedDto);

    void beforeSave(PatchPersistenceContext<Model> context);

    void afterSave(PatchPersistenceContext<Model> context);
}
