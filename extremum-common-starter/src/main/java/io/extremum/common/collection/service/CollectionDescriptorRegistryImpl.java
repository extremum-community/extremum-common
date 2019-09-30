package io.extremum.common.collection.service;

import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CollectionDescriptorRegistryImpl implements CollectionDescriptorRegistry {
    private final CollectionDescriptorService collectionDescriptorService;

    @Override
    public Descriptor freeCollection(String name) {
        CollectionDescriptor collectionDescriptor = CollectionDescriptor.forFree(name);
        return collectionDescriptorService.retrieveByCoordinatesOrCreate(collectionDescriptor);
    }
}
