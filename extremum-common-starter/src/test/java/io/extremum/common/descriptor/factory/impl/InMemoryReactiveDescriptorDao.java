package io.extremum.common.descriptor.factory.impl;

import io.extremum.common.descriptor.dao.ReactiveDescriptorDao;
import io.extremum.sharedmodels.descriptor.Descriptor;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryReactiveDescriptorDao implements ReactiveDescriptorDao {
    private final Map<String, Descriptor> descriptorMap = new ConcurrentHashMap<>();

    @Override
    public Mono<Descriptor> retrieveByExternalId(String externalId) {
        return Mono.justOrEmpty(descriptorMap.get(externalId));
    }

    @Override
    public Mono<Descriptor> retrieveByInternalId(String internalId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Mono<Descriptor> retrieveByCollectionCoordinates(String collectionCoordinates) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Mono<Descriptor> store(Descriptor descriptor) {
        descriptorMap.put(descriptor.getExternalId(), descriptor);
        return Mono.just(descriptor);
    }
}
