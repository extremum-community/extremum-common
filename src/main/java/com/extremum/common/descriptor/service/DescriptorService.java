package com.extremum.common.descriptor.service;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.dao.DescriptorDao;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class DescriptorService {

    private static DescriptorDao descriptorDao;

    public static void setDescriptorDao(DescriptorDao descriptorDao) {
        DescriptorService.descriptorDao = descriptorDao;
    }

    public static String createExternalId() {
        return UUID.randomUUID().toString();
    }

    public static Descriptor store(Descriptor descriptor) {
        return descriptorDao.store(descriptor);
    }

    public static Optional<Descriptor> loadByExternalId(String uuid) {
        return descriptorDao.retrieveByExternalId(uuid);
    }

    public static Optional<Descriptor> loadByInternalId(String internalId) {
        return descriptorDao.retrieveByInternalId(internalId);
    }

    public static Map<String, String> loadMapByExternalIds(Collection<String> externalIds) {
        Objects.requireNonNull(externalIds, "List of external ids can't be null");
        return descriptorDao.retrieveMapByExternalIds(externalIds);
    }

    public static Map<String, String> loadMapByInternalIds(Collection<String> internalIds) {
        Objects.requireNonNull(internalIds, "List of internal ids can't be null");
        return descriptorDao.retrieveMapByInternalIds(internalIds);
    }
}
