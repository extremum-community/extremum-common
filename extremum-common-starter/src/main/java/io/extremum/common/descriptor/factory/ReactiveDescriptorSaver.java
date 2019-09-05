package io.extremum.common.descriptor.factory;

import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.common.descriptor.service.ReactiveDescriptorService;
import io.extremum.sharedmodels.descriptor.Descriptor;
import reactor.core.publisher.Mono;

public class ReactiveDescriptorSaver {
    private final ReactiveDescriptorService reactiveDescriptorService;

    private final DescriptorSavers savers;

    public ReactiveDescriptorSaver(DescriptorService descriptorService,
                                   ReactiveDescriptorService reactiveDescriptorService) {
        this.reactiveDescriptorService = reactiveDescriptorService;
        savers = new DescriptorSavers(descriptorService);
    }

    public Mono<Descriptor> createAndSaveReactively(String internalId, String modelType,
                                                    Descriptor.StorageType storageType) {
        Descriptor descriptor = savers.createDescriptor(internalId, modelType, storageType);

        return reactiveDescriptorService.store(descriptor);
    }
}
