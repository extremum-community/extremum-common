package io.extremum.common.collection.dao.impl;

import io.extremum.common.collection.CollectionDescriptor;
import org.redisson.api.map.MapLoader;

import java.util.stream.Collectors;

class CollectionDescriptorIdMapLoader implements MapLoader<String, CollectionDescriptor> {
    private final CollectionDescriptorRepository repository;

    CollectionDescriptorIdMapLoader(CollectionDescriptorRepository repository) {
        this.repository = repository;
    }

    @Override
    public CollectionDescriptor load(String key) {
        return repository.findByExternalId(key).orElse(null);
    }

    @Override
    public Iterable<String> loadAllKeys() {
        return repository.findAllExternalIds().stream()
                .map(CollectionDescriptor::getExternalId)
                .collect(Collectors.toList());
    }
}
