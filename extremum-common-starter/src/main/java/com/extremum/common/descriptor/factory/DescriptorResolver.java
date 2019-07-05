package com.extremum.common.descriptor.factory;

import com.extremum.sharedmodels.annotation.UsesStaticDependencies;
import com.extremum.sharedmodels.descriptor.Descriptor;

/**
 * @author rpuch
 */
public final class DescriptorResolver {

    @UsesStaticDependencies
    public static String resolve(Descriptor descriptor, Descriptor.StorageType storageType) {
        String internalId = descriptor.getInternalId();
        Descriptor.StorageType currentType = descriptor.getStorageType();

        if (currentType != storageType) {
            throw new IllegalStateException("Wrong descriptor storage type " + currentType);
        }

        return internalId;
    }

    private DescriptorResolver() {
    }
}
