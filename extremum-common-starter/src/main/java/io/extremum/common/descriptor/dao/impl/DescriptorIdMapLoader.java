package io.extremum.common.descriptor.dao.impl;

import io.extremum.sharedmodels.descriptor.Descriptor;
import org.redisson.api.map.MapLoader;

import java.util.stream.Collectors;

class DescriptorIdMapLoader implements MapLoader<String, Descriptor> {
    private final DescriptorRepository descriptorRepository;

    DescriptorIdMapLoader(DescriptorRepository descriptorRepository) {
        this.descriptorRepository = descriptorRepository;
    }

    @Override
    public Descriptor load(String key) {
        return descriptorRepository.findById(key).orElse(null);
    }

    @Override
    public Iterable<String> loadAllKeys() {
        return descriptorRepository.findAllExternalIds().stream()
                .map(Descriptor::getExternalId)
                .collect(Collectors.toList());
    }
}
