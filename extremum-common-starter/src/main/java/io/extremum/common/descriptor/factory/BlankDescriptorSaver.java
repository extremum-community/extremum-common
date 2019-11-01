package io.extremum.common.descriptor.factory;

import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.sharedmodels.descriptor.Descriptor;

import java.util.List;
import java.util.stream.Collectors;

public class BlankDescriptorSaver {
    private final DescriptorService descriptorService;

    private final DescriptorSavers savers;

    public BlankDescriptorSaver(DescriptorService descriptorService) {
        this.descriptorService = descriptorService;

        savers = new DescriptorSavers(descriptorService);
    }

    public List<Descriptor> createAndSaveBatchOfBlankDescriptors(List<String> internalIds, Descriptor.StorageType storageType) {
        List<Descriptor> descriptors = internalIds.stream()
                .map(internalId -> savers.createSingleDescriptor(internalId, storageType))
                .peek(descriptor -> descriptor.setReadiness(Descriptor.Readiness.BLANK))
                .collect(Collectors.toList());

        return descriptorService.storeBatch(descriptors);
    }
}
