package io.extremum.common.collection.service;

import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;
import reactor.core.publisher.Mono;

public interface ReactiveCollectionOverride {
    boolean supports(Descriptor descriptor);

    Mono<Descriptor.Type> typeForGetOperation(Descriptor descriptor);

    Mono<CollectionDescriptor> extractCollectionFromDescriptor(Descriptor descriptor);
}
