package com.extremum.common.descriptor.factory.impl;

import com.extremum.common.descriptor.Descriptor;
import org.springframework.stereotype.Component;
import com.extremum.common.descriptor.factory.DescriptorFactory;

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

@Component
public final class ElasticDescriptorFactory extends UUIDDescriptorFactory {
    @Override
    Descriptor.StorageType storageType() {
        return Descriptor.StorageType.ELASTIC;
    }
}
