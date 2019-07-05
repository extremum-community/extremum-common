package com.extremum.common.descriptor.factory.impl;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.factory.DescriptorFactory;
import com.extremum.common.descriptor.factory.DescriptorResolver;
import com.extremum.common.descriptor.factory.DescriptorSaver;

import java.util.UUID;

public abstract class UUIDDescriptorFacilities {
    private final DescriptorFactory descriptorFactory;
    private final DescriptorSaver descriptorSaver;

    protected UUIDDescriptorFacilities(DescriptorFactory descriptorFactory,
            DescriptorSaver descriptorSaver) {
        this.descriptorFactory = descriptorFactory;
        this.descriptorSaver = descriptorSaver;
    }

    public abstract Descriptor.StorageType storageType();

    public Descriptor create(UUID uuid, String modelType) {
        return descriptorSaver.createAndSave(uuid.toString(), modelType, storageType());
    }

    public Descriptor fromInternalId(UUID uuid) {
        return fromInternalId(uuid.toString());
    }

    public Descriptor fromInternalId(String uuid) {
        return descriptorFactory.fromInternalId(uuid, storageType());
    }

    public Descriptor fromInternalIdOrNull(String uuid) {
        return descriptorFactory.fromInternalIdOrNull(uuid, storageType());
    }

    public UUID resolve(Descriptor descriptor) {
        String internalId = DescriptorResolver.resolve(descriptor, storageType());
        return UUID.fromString(internalId);
    }
}
