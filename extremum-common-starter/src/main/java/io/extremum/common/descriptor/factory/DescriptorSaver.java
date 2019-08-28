package io.extremum.common.descriptor.factory;

import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DescriptorSaver {
    private final DescriptorService descriptorService;

    public Descriptor createAndSave(String internalId, String modelType, Descriptor.StorageType storageType) {
        Descriptor descriptor = Descriptor.builder()
                .externalId(descriptorService.createExternalId())
                .type(Descriptor.Type.SINGLE)
                .internalId(internalId)
                .modelType(modelType)
                .storageType(storageType)
                .build();

        return descriptorService.store(descriptor);
    }

    public Descriptor createAndSave(CollectionDescriptor collectionDescriptor) {
        Descriptor descriptor = Descriptor.forCollection(descriptorService.createExternalId(), collectionDescriptor);
        return descriptorService.store(descriptor);
    }
}
