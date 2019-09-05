package io.extremum.common.descriptor.factory;

import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.sharedmodels.descriptor.Descriptor;

class DescriptorSavers {
    private final DescriptorService descriptorService;

    DescriptorSavers(DescriptorService descriptorService) {
        this.descriptorService = descriptorService;
    }

    Descriptor createDescriptor(String internalId, String modelType, Descriptor.StorageType storageType) {
        return Descriptor.builder()
                .externalId(descriptorService.createExternalId())
                .type(Descriptor.Type.SINGLE)
                .internalId(internalId)
                .modelType(modelType)
                .storageType(storageType)
                .build();
    }
}
