package io.extremum.common.collection.dao;

import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import reactor.core.publisher.Mono;

/**
 * @author rpuch
 */
public interface ReactiveCollectionDescriptorDao {
    Mono<CollectionDescriptor> retrieveByExternalId(String externalId);
}
