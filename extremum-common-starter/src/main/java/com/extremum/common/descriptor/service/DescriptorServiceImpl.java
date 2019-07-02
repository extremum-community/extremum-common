package com.extremum.common.descriptor.service;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.dao.DescriptorDao;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public final class DescriptorServiceImpl implements DescriptorService {
    private final DescriptorDao descriptorDao;

    private static volatile DescriptorService instance;

    public static DescriptorService getInstance() {
        return instance;
    }

    public static void setInstance(DescriptorService instance) {
        DescriptorServiceImpl.instance = instance;
    }

    public static String createExternalId() {
        return UUID.randomUUID().toString();
    }

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
