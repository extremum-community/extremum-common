package com.extremum.common.descriptor.factory;

import com.extremum.sharedmodels.descriptor.Descriptor;
import org.apache.commons.lang3.StringUtils;

public final class DescriptorFactory {
    public Descriptor fromExternalId(String externalId) {
        return externalId != null ? new Descriptor(externalId) : null;
    }

    public Descriptor fromInternalIdOfUnknownType(String internalId) {
        return fromInternalIdOrNull(internalId, null);
    }

    public Descriptor fromInternalIdOrNull(String internalId, Descriptor.StorageType storageType) {
        return StringUtils.isBlank(internalId) ? null :
                Descriptor.builder().internalId(internalId).storageType(storageType).build();
    }

    public Descriptor fromInternalId(String internalId, Descriptor.StorageType storageType) {
        if (StringUtils.isBlank(internalId)) {
            throw new IllegalArgumentException("Empty internal id detected");
        }
        return Descriptor.builder()
                .internalId(internalId)
                .storageType(storageType)
                .build();
    }
}
