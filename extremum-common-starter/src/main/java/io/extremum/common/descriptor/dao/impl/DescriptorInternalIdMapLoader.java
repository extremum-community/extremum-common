package io.extremum.common.descriptor.dao.impl;

import io.extremum.sharedmodels.descriptor.Descriptor;
import org.redisson.api.map.MapLoader;

import java.util.stream.Collectors;

class DescriptorInternalIdMapLoader implements MapLoader<String, String> {
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

    @Override
    public Iterable<String> loadAllKeys() {
        return descriptorRepository.findAllInternalIds().stream()
                .map(Descriptor::getInternalId)
                .collect(Collectors.toList());
    }
}
