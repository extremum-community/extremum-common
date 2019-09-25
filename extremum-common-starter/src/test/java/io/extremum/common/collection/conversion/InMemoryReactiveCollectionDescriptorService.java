package io.extremum.common.collection.conversion;

import io.extremum.common.collection.service.ReactiveCollectionDescriptorService;
import io.extremum.common.descriptor.factory.impl.InMemoryReactiveDescriptorService;
import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;
import reactor.core.publisher.Mono;

/**
 * @author rpuch
 */
public class InMemoryReactiveCollectionDescriptorService implements ReactiveCollectionDescriptorService {
    private final InMemoryReactiveDescriptorService reactiveDescriptorService;

    public InMemoryReactiveCollectionDescriptorService(InMemoryReactiveDescriptorService reactiveDescriptorService) {
        this.reactiveDescriptorService = reactiveDescriptorService;
    }

    @Override
    public Mono<CollectionDescriptor> retrieveByExternalId(String externalId) {
        return reactiveDescriptorService.loadByExternalId(externalId).map(Descriptor::getCollection);
    }

    @Override
    public Mono<Descriptor> retrieveByCoordinates(String coordinatesString) {
        Descriptor descriptorOrNull = reactiveDescriptorService.descriptors()
                .filter(descriptor -> descriptor.effectiveType() == Descriptor.Type.COLLECTION)
                .filter(descriptor -> descriptor.getCollection().toCoordinatesString().equals(coordinatesString))
                .findAny()
                .orElse(null);
        return Mono.justOrEmpty(descriptorOrNull);
    }

    @Override
    public Mono<Descriptor> retrieveByCoordinatesOrCreate(CollectionDescriptor collectionDescriptor) {
        throw new UnsupportedOperationException();
    }
}
