package io.extremum.common.collection.dao.impl;

import io.extremum.common.collection.CollectionDescriptor;
import io.extremum.common.descriptor.dao.impl.ModestMapLoader;

class CollectionDescriptorIdMapLoader extends ModestMapLoader<String, CollectionDescriptor> {
    private final CollectionDescriptorRepository repository;

    CollectionDescriptorIdMapLoader(CollectionDescriptorRepository repository) {
        this.repository = repository;
    }

    @Override
    public CollectionDescriptor load(String key) {
        return repository.findByExternalId(key).orElse(null);
    }
}
