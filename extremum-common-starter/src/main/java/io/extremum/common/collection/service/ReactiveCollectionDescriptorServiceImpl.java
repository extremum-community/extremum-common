package io.extremum.common.collection.service;

import io.extremum.common.collection.CollectionDescriptor;
import reactor.core.publisher.Mono;

public class ReactiveCollectionDescriptorServiceImpl implements ReactiveCollectionDescriptorService {
    @Override
    public Mono<CollectionDescriptor> retrieveByExternalId(String externalId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
