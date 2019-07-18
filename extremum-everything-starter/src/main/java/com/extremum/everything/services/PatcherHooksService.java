package com.extremum.everything.services;

import com.extremum.common.models.Model;
import com.extremum.sharedmodels.dto.RequestDto;

/**
 * @author rpuch
 */
public interface PatcherHooksService<M extends Model, D extends RequestDto> extends EverythingEverythingService {
    default D afterPatchAppliedToDto(D dto) {
        return dto;
    }

    default void beforeSave(PatchPersistenceContext<M> context) {
    }

    default void afterSave(PatchPersistenceContext<M> context) {
    }
}
