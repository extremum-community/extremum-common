package io.extremum.common.descriptor.factory;

import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.common.descriptor.service.ReactiveDescriptorService;
import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
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

    public Mono<Descriptor> createAndSave(String internalId, String modelType,
                                          Descriptor.StorageType storageType) {
        Descriptor descriptor = savers.createSingleDescriptor(internalId, modelType, storageType);

        return reactiveDescriptorService.store(descriptor);
    }

    public Mono<Descriptor> createAndSave(CollectionDescriptor collectionDescriptor) {
        Descriptor descriptor = savers.createCollectionDescriptor(collectionDescriptor);
        return reactiveDescriptorService.store(descriptor);
    }
}
