package io.extremum.common.descriptor.factory;

import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.common.descriptor.service.ReactiveDescriptorService;
import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.StorageType;
import org.springframework.dao.DuplicateKeyException;
import reactor.core.publisher.Mono;

public class ReactiveDescriptorSaver {
    private final ReactiveDescriptorService reactiveDescriptorService;

    private final DescriptorSavers savers;

    public ReactiveDescriptorSaver(DescriptorService descriptorService,
                                   ReactiveDescriptorService reactiveDescriptorService) {
        this.reactiveDescriptorService = reactiveDescriptorService;
        savers = new DescriptorSavers(descriptorService);
    }

    public Mono<Descriptor> createAndSave(String internalId, String modelType, StorageType storageType) {
        Descriptor descriptor = savers.createSingleDescriptor(internalId, modelType, storageType);

        return createOrGet(descriptor);
    }

    public Mono<Descriptor> createAndSave(CollectionDescriptor collectionDescriptor) {
        Descriptor descriptor = savers.createCollectionDescriptor(collectionDescriptor);
        return createOrGet(descriptor);
    }

    private Mono<Descriptor> createOrGet(Descriptor descriptor) {
        return reactiveDescriptorService.store(descriptor)
                .onErrorResume(DuplicateKeyException.class,
                        ex -> reactiveDescriptorService.loadByInternalId(descriptor.getInternalId()))
                .switchIfEmpty(Mono.defer(() -> Mono.error(
                        new IllegalStateException(String.format("Could not insert nor retrive by internal ID: " +
                                "something is wrong! External ID is %s, internal ID is %s",
                                descriptor.getExternalId(), descriptor.getInternalId()))
                        ))
                );
    }
}
