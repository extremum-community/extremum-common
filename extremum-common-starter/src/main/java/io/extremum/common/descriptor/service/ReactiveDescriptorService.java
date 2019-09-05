package io.extremum.common.descriptor.service;

import io.extremum.sharedmodels.descriptor.Descriptor;
import reactor.core.publisher.Mono;

public interface ReactiveDescriptorService {
    Mono<Descriptor> store(Descriptor descriptor);

    Mono<Descriptor> loadByInternalId(String internalId);
}
