package com.extremum.common.descriptor.service;

import com.extremum.sharedmodels.descriptor.Descriptor;
import com.extremum.common.descriptor.dao.DescriptorDao;
import com.extremum.common.uuid.UUIDGenerator;
import lombok.RequiredArgsConstructor;

import java.util.*;

@RequiredArgsConstructor
public final class DescriptorServiceImpl implements DescriptorService {
    private final DescriptorDao descriptorDao;
    private final UUIDGenerator uuidGenerator;

    @Override
    public String createExternalId() {
        return uuidGenerator.generateUUID();
    }

    @Override
    public Descriptor store(Descriptor descriptor) {
        return descriptorDao.store(descriptor);
    }

    @Override
    public Optional<Descriptor> loadByExternalId(String externalId) {
        return descriptorDao.retrieveByExternalId(externalId);
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
