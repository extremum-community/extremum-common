package io.extremum.common.collection.service;

import io.extremum.common.collection.CollectionDescriptor;
import reactor.core.publisher.Mono;

/**
 * @author rpuch
 */
public interface ReactiveCollectionDescriptorService {
    Mono<CollectionDescriptor> retrieveByExternalId(String externalId);
}
