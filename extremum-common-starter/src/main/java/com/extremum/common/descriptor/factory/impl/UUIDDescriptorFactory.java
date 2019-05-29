package com.extremum.common.descriptor.factory.impl;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.factory.DescriptorFactory;

import java.util.UUID;

public abstract class UUIDDescriptorFactory extends DescriptorFactory {
    public abstract Descriptor.StorageType storageType();

    public Descriptor create(UUID uuid, String modelType) {
        return DescriptorFactory.create(uuid.toString(), modelType, storageType());
    }

    public Descriptor fromInternalId(UUID uuid) {
        return fromInternalId(uuid.toString());
    }

    public Descriptor fromInternalId(String uuid) {
        return DescriptorFactory.fromInternalId(uuid, storageType());
    }

    public Descriptor fromInternalIdOrNull(String uuid) {
        return DescriptorFactory.fromInternalIdOrNull(uuid, storageType());
    }

    public UUID resolve(Descriptor descriptor) {
        String internalId = DescriptorFactory.resolve(descriptor, storageType());
        return UUID.fromString(internalId);
    }
}
