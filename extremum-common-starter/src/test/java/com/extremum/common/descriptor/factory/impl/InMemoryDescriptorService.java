package com.extremum.common.descriptor.factory.impl;

import com.extremum.common.descriptor.service.DescriptorService;
import com.extremum.common.utils.ReflectionUtils;
import com.extremum.sharedmodels.descriptor.Descriptor;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * @author rpuch
 */
public class InMemoryDescriptorService implements DescriptorService {
    @Override
    public String createExternalId() {
        return UUID.randomUUID().toString();
    }

    @Override
    public Descriptor store(Descriptor descriptor) {
        String externalId = ReflectionUtils.getFieldValue(descriptor, "externalId");
        if (externalId == null) {
            ReflectionUtils.setFieldValue(descriptor, "externalId", UUID.randomUUID().toString());
        }
        return descriptor;
    }

    @Override
    public Optional<Descriptor> loadByExternalId(String externalId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<Descriptor> loadByInternalId(String internalId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, String> loadMapByExternalIds(Collection<String> externalIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, String> loadMapByInternalIds(Collection<String> internalIds) {
        throw new UnsupportedOperationException();
    }
}
