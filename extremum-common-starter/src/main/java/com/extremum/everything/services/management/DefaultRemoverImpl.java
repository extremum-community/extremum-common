package com.extremum.everything.services.management;

import com.extremum.common.models.Model;
import com.extremum.everything.config.listener.ModelClasses;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultRemoverImpl implements DefaultRemover {
    private final CommonServices commonServices;
    private final ModelClasses modelClasses;

    @Override
    public void remove(String id) {
        Class<? extends Model> modelClass = modelClasses.getModelClassByDescriptorId(id);
        commonServices.findServiceByModel(modelClass).delete(id);
    }
}
