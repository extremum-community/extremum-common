package io.extremum.common.descriptor.service;

import io.extremum.sharedmodels.descriptor.Descriptor;
import reactor.core.publisher.Mono;

public interface ReactiveDescriptorService {
    Mono<Descriptor> loadByInternalId(String internalId);
}
