package io.extremum.everything.support;

import io.extremum.common.support.ModelClasses;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.DescriptorNotFoundException;
import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.common.models.Model;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultModelDescriptors implements ModelDescriptors {
    private final ModelClasses modelClasses;
    private final DescriptorService descriptorService;

    @Override
    public <M extends Model> Class<M> getModelClassByModelInternalId(String internalId) {
        Descriptor descriptor = descriptorService.loadByInternalId(internalId)
                .orElseThrow(() -> new DescriptorNotFoundException("For internal id: " + internalId));

        return modelClasses.getClassByModelName(descriptor.getModelType());
    }
}
