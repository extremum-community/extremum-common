package io.extremum.common.descriptor.factory;

import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;

public class DescriptorSavers {
    private final DescriptorService descriptorService;

    public DescriptorSavers(DescriptorService descriptorService) {
        this.descriptorService = descriptorService;
    }

    public Descriptor createSingleDescriptor(String internalId, Descriptor.StorageType storageType) {
        return createSingleDescriptor(internalId, null, storageType);
    }

    Descriptor createSingleDescriptor(String internalId, String modelType, Descriptor.StorageType storageType) {
        return Descriptor.builder()
                .externalId(descriptorService.createExternalId())
                .type(Descriptor.Type.SINGLE)
                .internalId(internalId)
                .modelType(modelType)
                .storageType(storageType)
                .build();
    }

    public Descriptor createCollectionDescriptor(CollectionDescriptor collectionDescriptor) {
        return Descriptor.forCollection(descriptorService.createExternalId(), collectionDescriptor);
    }
}
