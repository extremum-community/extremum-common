package io.extremum.common.collection.service;

import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import reactor.core.publisher.Mono;

/**
 * @author rpuch
 */
public interface ReactiveCollectionDescriptorService {
    Mono<CollectionDescriptor> retrieveByExternalId(String externalId);
}
