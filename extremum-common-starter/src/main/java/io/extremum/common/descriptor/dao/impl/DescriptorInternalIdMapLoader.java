package io.extremum.common.descriptor.dao.impl;

import io.extremum.sharedmodels.descriptor.Descriptor;

class DescriptorInternalIdMapLoader extends CarefulMapLoader<String, String> {
    private final DescriptorRepository descriptorRepository;

    DescriptorInternalIdMapLoader(DescriptorRepository descriptorRepository) {
        this.descriptorRepository = descriptorRepository;
    }

    @Override
    public String load(String key) {
        return descriptorRepository.findByInternalId(key)
                .map(Descriptor::getExternalId)
                .orElse(null);
    }
}
