package io.extremum.common.collection.service;

import io.extremum.sharedmodels.descriptor.Descriptor;
import reactor.core.publisher.Mono;

public interface ReactiveCollectionDescriptorRegistry {
    Mono<Descriptor> freeCollection(String name);
}
