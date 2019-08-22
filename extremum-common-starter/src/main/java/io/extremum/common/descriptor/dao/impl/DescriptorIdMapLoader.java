package io.extremum.common.descriptor.dao.impl;

import io.extremum.sharedmodels.descriptor.Descriptor;

class DescriptorIdMapLoader extends ModestMapLoader<String, Descriptor> {
    private final DescriptorRepository descriptorRepository;

    DescriptorIdMapLoader(DescriptorRepository descriptorRepository) {
        this.descriptorRepository = descriptorRepository;
    }

    @Override
    public Descriptor load(String key) {
        return descriptorRepository.findById(key).orElse(null);
    }
}
