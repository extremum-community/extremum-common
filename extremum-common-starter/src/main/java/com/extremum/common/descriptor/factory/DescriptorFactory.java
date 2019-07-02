package com.extremum.common.descriptor.factory;

import com.extremum.common.descriptor.Descriptor;
import org.apache.commons.lang3.StringUtils;

public class DescriptorFactory {
    public static Descriptor fromExternalId(String externalId) {
        return externalId != null ? new Descriptor(externalId) : null;
    }

    public static Descriptor fromInternalIdOfUnknownType(String internalId) {
        return fromInternalIdOrNull(internalId, null);
    }

    public static Descriptor fromInternalIdOrNull(String internalId, Descriptor.StorageType storageType) {
        return StringUtils.isBlank(internalId) ? null :
                Descriptor.builder().internalId(internalId).storageType(storageType).build();
    }

    public static Descriptor fromInternalId(String internalId, Descriptor.StorageType storageType) {
        if (StringUtils.isBlank(internalId)) {
            throw new IllegalArgumentException("Empty internal id detected");
        }
        return Descriptor.builder()
                .internalId(internalId)
                .storageType(storageType)
                .build();
    }

    public static String resolve(Descriptor descriptor, Descriptor.StorageType storageType) {
        String internalId = descriptor.getInternalId();
        Descriptor.StorageType currentType = descriptor.getStorageType();

        if (currentType != storageType) {
            throw new IllegalStateException("Wrong descriptor storage type " + currentType);
        }

        return internalId;
    }
}
