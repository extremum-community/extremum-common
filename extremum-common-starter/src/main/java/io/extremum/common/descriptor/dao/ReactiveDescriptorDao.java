package io.extremum.common.descriptor.dao;

import io.extremum.sharedmodels.descriptor.Descriptor;
import reactor.core.publisher.Mono;

public interface ReactiveDescriptorDao {
    Mono<Descriptor> retrieveByInternalId(String internalId);
}
