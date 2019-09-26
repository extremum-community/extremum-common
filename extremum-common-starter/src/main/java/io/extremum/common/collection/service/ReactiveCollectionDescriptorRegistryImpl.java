package io.extremum.common.collection.service;

import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@AllArgsConstructor
public class ReactiveCollectionDescriptorRegistryImpl implements ReactiveCollectionDescriptorRegistry {
    private final ReactiveCollectionDescriptorService reactiveCollectionDescriptorService;

    @Override
    public Mono<Descriptor> freeCollection(String name) {
        CollectionDescriptor collectionDescriptor = CollectionDescriptor.forFree(name);
        return reactiveCollectionDescriptorService.retrieveByCoordinatesOrCreate(collectionDescriptor);
    }
}
