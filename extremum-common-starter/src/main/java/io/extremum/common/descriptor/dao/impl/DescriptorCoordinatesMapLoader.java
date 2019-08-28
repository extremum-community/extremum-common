package io.extremum.common.descriptor.dao.impl;

import io.extremum.sharedmodels.descriptor.Descriptor;

class DescriptorCoordinatesMapLoader extends CarefulMapLoader<String, String> {
    private final DescriptorRepository repository;

    DescriptorCoordinatesMapLoader(DescriptorRepository repository) {
        this.repository = repository;
    }

    @Override
    public String load(String key) {
        return repository.findByCollectionCoordinatesString(key)
                .map(Descriptor::getExternalId)
                .orElse(null);
    }
}
