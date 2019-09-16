package io.extremum.common.descriptor.factory.impl;

import io.extremum.common.descriptor.factory.DescriptorResolver;
import io.extremum.common.descriptor.factory.ReactiveDescriptorSaver;
import io.extremum.sharedmodels.descriptor.Descriptor;
import reactor.core.publisher.Mono;

import java.util.UUID;

public abstract class ReactiveUUIDDescriptorFacilities {
    private final ReactiveDescriptorSaver descriptorSaver;

    protected ReactiveUUIDDescriptorFacilities(ReactiveDescriptorSaver descriptorSaver) {
        this.descriptorSaver = descriptorSaver;
    }

    protected abstract Descriptor.StorageType storageType();

    public Mono<Descriptor> create(UUID uuid, String modelType) {
        return descriptorSaver.createAndSave(uuid.toString(), modelType, storageType());
    }

    public Mono<UUID> resolve(Descriptor descriptor) {
        return DescriptorResolver.resolveReactively(descriptor, storageType())
                .map(UUID::fromString);
    }
}
