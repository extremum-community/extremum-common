package com.extremum.common.descriptor.factory;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.service.DescriptorService;
import org.apache.commons.lang3.StringUtils;

import java.util.UUID;

public class DescriptorFactory {

    public static Descriptor fromExternalId(String externalId) {
        return externalId != null ? new Descriptor(externalId) : null;
    }

    public static Descriptor fromInternalIdOfUnknownType(String internalId) {
        return fromInternalIdOrNull(internalId, null);
    }

    protected static Descriptor fromInternalIdOrNull(String internalId, Descriptor.StorageType storageType) {
        return StringUtils.isBlank(internalId) ? null : new Descriptor(null, internalId, null, storageType);
    }

    protected static Descriptor fromInternalId(String internalId, Descriptor.StorageType storageType) {
        if (StringUtils.isBlank(internalId)) {
            throw new IllegalArgumentException("Empty internal id detected");
        }
        return new Descriptor(null, internalId, null, storageType);
    }

    protected static String resolve(Descriptor descriptor, Descriptor.StorageType storageType) {
        String internalId = descriptor.getInternalId();
        Descriptor.StorageType currentType = descriptor.getStorageType();

        if (currentType != storageType) {
            throw new IllegalStateException("Wrong descriptor storage type " + currentType);
        }

        return internalId;
    }

    protected static Descriptor create(UUID uuid, Descriptor.StorageType storageType) {
        Descriptor descriptor = new Descriptor(
                DescriptorService.createExternalId(),
                uuid.toString(),
                storageType
        );
        return DescriptorService.store(descriptor);
    }

    protected static Descriptor create(String internalId, String modelType, Descriptor.StorageType storageType) {
        Descriptor descriptor = new Descriptor(
                DescriptorService.createExternalId(),
                internalId,
                modelType,
                storageType
        );
        return DescriptorService.store(descriptor);
    }
}
