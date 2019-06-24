package com.extremum.everything.services.management;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.exceptions.DescriptorNotFoundException;
import com.extremum.common.descriptor.service.DescriptorService;
import com.extremum.common.models.Model;
import com.extremum.everything.config.listener.ModelClasses;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultRemoverImpl implements DefaultRemover {
    private final CommonServices commonServices;
    private final ModelClasses modelClasses;

    @Override
    public void remove(String id) {
        Class<? extends Model> modelClass = getModelClassByDescriptorId(id);
        commonServices.findServiceByModel(modelClass).delete(id);
    }

    private Class<? extends Model> getModelClassByDescriptorId(String internalId) {
        Descriptor descriptor = DescriptorService.loadByInternalId(internalId)
                .orElseThrow(() -> new DescriptorNotFoundException("For internal id: " + internalId));

        return modelClasses.getClassByModelName(descriptor.getModelType());
    }
}
