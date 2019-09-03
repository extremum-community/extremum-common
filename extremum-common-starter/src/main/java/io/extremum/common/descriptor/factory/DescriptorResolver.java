package io.extremum.common.descriptor.factory;

import io.extremum.sharedmodels.annotation.UsesStaticDependencies;
import io.extremum.sharedmodels.descriptor.Descriptor;

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
