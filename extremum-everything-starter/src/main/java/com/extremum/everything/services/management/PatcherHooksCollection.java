package com.extremum.everything.services.management;

import com.extremum.common.models.Model;
import com.extremum.common.modelservices.ModelServices;
import com.extremum.everything.services.PatchPersistenceContext;
import com.extremum.everything.services.PatcherHooksService;
import com.extremum.sharedmodels.dto.RequestDto;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * @author rpuch
 */
@RequiredArgsConstructor
public class PatcherHooksCollection {
    private final List<PatcherHooksService<?, ?>> patcherHooksServices;

    public RequestDto afterPatchAppliedToDto(String modelName, RequestDto patchedDto) {
        PatcherHooks hooks = getHooks(modelName);
        return hooks.afterPatchAppliedToDto(patchedDto);
    }

    private PatcherHooks getHooks(String modelName) {
        @SuppressWarnings("unchecked")
        PatcherHooksService<Model, RequestDto> service =
                (PatcherHooksService<Model, RequestDto>) ModelServices.findServiceForModel(
                        modelName, patcherHooksServices);
        if (service != null) {
            return new NonDefaultPatcherHooks(service);
        } else {
            return new NullPatcherHooks();
        }
    }

    public void beforeSave(String modelName, PatchPersistenceContext<? extends Model> context) {
        PatcherHooks hooks = getHooks(modelName);
        hooks.beforeSave((PatchPersistenceContext<Model>) context);
    }

    public void afterSave(String modelName, PatchPersistenceContext<? extends Model> context) {
        PatcherHooks hooks = getHooks(modelName);
        hooks.afterSave((PatchPersistenceContext<Model>) context);
    }
}
