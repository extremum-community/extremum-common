package com.extremum.common.descriptor.service;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.dao.DescriptorDao;
import lombok.RequiredArgsConstructor;

import java.util.*;

@RequiredArgsConstructor
public final class DescriptorServiceImpl implements DescriptorService {
    private final DescriptorDao descriptorDao;

    @Override
    public Descriptor store(Descriptor descriptor) {
        return descriptorDao.store(descriptor);
    }

    @Override
    public Optional<Descriptor> loadByExternalId(String uuid) {
        return descriptorDao.retrieveByExternalId(uuid);
    }

    @Override
    public Optional<Descriptor> loadByInternalId(String internalId) {
        return descriptorDao.retrieveByInternalId(internalId);
    }

    @Override
    public Map<String, String> loadMapByExternalIds(Collection<String> externalIds) {
        Objects.requireNonNull(externalIds, "List of external ids can't be null");
        return descriptorDao.retrieveMapByExternalIds(externalIds);
    }

    @Override
    public Map<String, String> loadMapByInternalIds(Collection<String> internalIds) {
        Objects.requireNonNull(internalIds, "List of internal ids can't be null");
        return descriptorDao.retrieveMapByInternalIds(internalIds);
    }
}
