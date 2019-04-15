package com.extremum.common.descriptor.dao.impl;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.dao.DescriptorDao;
import org.redisson.api.RMap;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toMap;


public abstract class BaseDescriptorDao implements DescriptorDao {

    private final RMap<String, Descriptor> descriptors;
    private final RMap<String, String> internalIdIndex;

    public BaseDescriptorDao(RMap<String, Descriptor> descriptors, RMap<String, String> internalIdIndex) {
        this.descriptors = descriptors;
        this.internalIdIndex = internalIdIndex;
    }

    @Override
    public Optional<Descriptor> retrieveByExternalId(String externalId) {
        return Optional.ofNullable(descriptors.get(externalId));
    }

    @Override
    public Optional<Descriptor> retrieveByInternalId(String internalId) {
        String descriptorId = internalIdIndex.get(internalId);

        return Optional.ofNullable(descriptorId).map(descriptors::get);
    }

    @Override
    public Map<String, String> retrieveMapByExternalIds(@NotNull Collection<String> externalIds) {
        Map<String, Descriptor> all = descriptors.getAll(new HashSet<>(externalIds));

        return all.entrySet().stream()
                .collect(toMap(Map.Entry::getKey, e -> e.getValue().getInternalId()));
    }

    @Override
    public Map<String, String> retrieveMapByInternalIds(Collection<String> internalIds) {
        return internalIdIndex.getAll(new HashSet<>(internalIds));
    }

    @Override
    public Descriptor store(Descriptor descriptor) {
        if (!internalIdIndex.containsKey(descriptor.getInternalId())) {
            descriptors.put(descriptor.getExternalId(), descriptor);
            internalIdIndex.put(descriptor.getInternalId(), descriptor.getExternalId());
        }
        return descriptor;
    }
}
