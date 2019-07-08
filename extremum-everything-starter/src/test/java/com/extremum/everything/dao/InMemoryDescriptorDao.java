package com.extremum.everything.dao;

import com.extremum.sharedmodels.descriptor.Descriptor;
import com.extremum.common.descriptor.dao.DescriptorDao;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author rpuch
 */
class InMemoryDescriptorDao implements DescriptorDao {
    private final ConcurrentMap<String, Descriptor> descriptors = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, String> internalToExternalId = new ConcurrentHashMap<>();

    @Override
    public Optional<Descriptor> retrieveByExternalId(String externalId) {
        return Optional.ofNullable(descriptors.get(externalId));
    }

    @Override
    public Optional<Descriptor> retrieveByInternalId(String internalId) {
        return Optional.ofNullable(internalToExternalId.get(internalId))
                .flatMap(this::retrieveByExternalId);
    }

    @Override
    public Map<String, String> retrieveMapByExternalIds(Collection<String> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, String> retrieveMapByInternalIds(Collection<String> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Descriptor store(Descriptor descriptor) {
        descriptors.put(descriptor.getExternalId(), descriptor);
        internalToExternalId.put(descriptor.getInternalId(), descriptor.getExternalId());
        return descriptor;
    }
}
