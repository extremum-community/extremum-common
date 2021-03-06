package io.extremum.common.descriptor.factory;

import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.StorageType;

public class DescriptorSavers {
    private final DescriptorService descriptorService;

    public DescriptorSavers(DescriptorService descriptorService) {
        this.descriptorService = descriptorService;
    }

    public Descriptor createSingleDescriptor(String internalId, StorageType storageType) {
        return createSingleDescriptor(internalId, null, storageType);
    }

    Descriptor createSingleDescriptor(String internalId, String modelType, StorageType storageType) {
        return Descriptor.builder()
                .externalId(descriptorService.createExternalId())
                .type(Descriptor.Type.SINGLE)
                .readiness(Descriptor.Readiness.READY)
                .internalId(internalId)
                .modelType(modelType)
                .storageType(storageType)
                .build();
    }

    public Descriptor createCollectionDescriptor(CollectionDescriptor collectionDescriptor) {
        return Descriptor.forCollection(descriptorService.createExternalId(), collectionDescriptor);
    }
}
