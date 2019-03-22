package com.extremum.common.descriptor.factory.impl;

import com.extremum.common.descriptor.factory.DescriptorFactory;
import com.extremum.common.descriptor.Descriptor;

import java.util.UUID;

public final class ElasticDescriptorFactory extends DescriptorFactory {

    private static Descriptor.StorageType storageType = Descriptor.StorageType.ELASTIC;

    private ElasticDescriptorFactory() {}

    public static Descriptor create(UUID uuid, String modelType) {
        return DescriptorFactory.create(uuid.toString(), modelType, storageType);
    }

    public static Descriptor fromInternalId(UUID uuid) {
        return fromInternalId(uuid.toString());
    }

    public static Descriptor fromInternalId(String uuid) {
        return DescriptorFactory.fromInternalId(uuid, storageType);
    }

    public static Descriptor fromInternalIdOrNull(String uuid) {
        return DescriptorFactory.fromInternalIdOrNull(uuid, storageType);
    }

    public static UUID resolve(Descriptor descriptor) {
        String internalId = DescriptorFactory.resolve(descriptor, storageType);
        return UUID.fromString(internalId);
    }
}
