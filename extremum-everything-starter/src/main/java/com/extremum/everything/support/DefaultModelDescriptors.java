package com.extremum.everything.support;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.exceptions.DescriptorNotFoundException;
import com.extremum.common.descriptor.service.DescriptorService;
import com.extremum.common.models.Model;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultModelDescriptors implements ModelDescriptors {
    private final ModelClasses modelClasses;
    private final DescriptorService descriptorService;

    @Override
    public <M extends Model> Class<M> getModelClassByDescriptorId(String internalId) {
        Descriptor descriptor = descriptorService.loadByInternalId(internalId)
                .orElseThrow(() -> new DescriptorNotFoundException("For internal id: " + internalId));

        return modelClasses.getClassByModelName(descriptor.getModelType());
    }
}
