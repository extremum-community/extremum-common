package io.extremum.common.descriptor.factory;

import io.extremum.sharedmodels.annotation.UsesStaticDependencies;
import io.extremum.sharedmodels.descriptor.Descriptor;
import reactor.core.publisher.Mono;

/**
 * @author rpuch
 */
public final class DescriptorResolver {

    @UsesStaticDependencies
    public static String resolve(Descriptor descriptor, Descriptor.StorageType expectedType) {
        String internalId = descriptor.getInternalId();
        Descriptor.StorageType currentType = descriptor.getStorageType();

        makeSureStorageTypesMatch(expectedType, currentType);

        return internalId;
    }

    private static void makeSureStorageTypesMatch(Descriptor.StorageType expectedType,
                                                  Descriptor.StorageType actualType) {
        if (actualType != expectedType) {
            throw new IllegalStateException("Wrong descriptor storage type " + actualType);
        }
    }

    @UsesStaticDependencies
    public static Mono<String> resolveReactively(Descriptor descriptor, Descriptor.StorageType expectedType) {
        return descriptor.getStorageTypeReactively()
                .doOnNext(currentType -> makeSureStorageTypesMatch(expectedType, currentType))
                .then(descriptor.getInternalIdReactively());
    }

    private DescriptorResolver() {
    }
}
