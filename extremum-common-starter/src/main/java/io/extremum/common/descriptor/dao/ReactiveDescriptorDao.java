package io.extremum.common.descriptor.dao;

import io.extremum.sharedmodels.descriptor.Descriptor;
import reactor.core.publisher.Mono;

public interface ReactiveDescriptorDao {
    Mono<Descriptor> retrieveByExternalId(String externalId);

    Mono<Descriptor> retrieveByInternalId(String internalId);

    Mono<Descriptor> store(Descriptor descriptor);

}
