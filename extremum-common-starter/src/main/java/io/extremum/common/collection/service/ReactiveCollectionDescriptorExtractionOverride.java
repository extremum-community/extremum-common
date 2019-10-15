package io.extremum.common.collection.service;

import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;
import reactor.core.publisher.Mono;

public interface ReactiveCollectionDescriptorExtractionOverride {
    boolean supports(Descriptor descriptor);

    Mono<CollectionDescriptor> extractCollectionFromDescriptor(Descriptor descriptor);
}
