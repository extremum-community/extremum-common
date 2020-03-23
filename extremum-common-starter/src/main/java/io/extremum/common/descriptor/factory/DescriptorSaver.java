package io.extremum.common.descriptor.factory;

import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.StorageType;

public class DescriptorSaver {
    private final DescriptorService descriptorService;

    private final DescriptorSavers savers;

    public DescriptorSaver(DescriptorService descriptorService) {
        this.descriptorService = descriptorService;
        savers = new DescriptorSavers(descriptorService);
    }

    public Descriptor createAndSave(String internalId, String modelType, StorageType storageType) {
        Descriptor descriptor = savers.createSingleDescriptor(internalId, modelType, storageType);

        return descriptorService.store(descriptor);
    }

    public Descriptor createAndSave(CollectionDescriptor collectionDescriptor) {
        Descriptor descriptor = Descriptor.forCollection(descriptorService.createExternalId(), collectionDescriptor);
        return descriptorService.store(descriptor);
    }
}
