package io.extremum.common.descriptor.factory;

import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.common.descriptor.service.DescriptorService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DescriptorSaver {
    private final DescriptorService descriptorService;

    public Descriptor createAndSave(String internalId, String modelType, Descriptor.StorageType storageType) {
        Descriptor descriptor = Descriptor.builder()
                .externalId(descriptorService.createExternalId())
                .internalId(internalId)
                .modelType(modelType)
                .storageType(storageType)
                .build();

        return descriptorService.store(descriptor);
    }
}