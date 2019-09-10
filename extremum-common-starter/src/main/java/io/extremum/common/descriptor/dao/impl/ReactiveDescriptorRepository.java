package io.extremum.common.descriptor.dao.impl;

import io.extremum.sharedmodels.descriptor.Descriptor;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

/**
 * @author rpuch
 */
@Repository
public interface ReactiveDescriptorRepository extends ReactiveMongoRepository<Descriptor, String> {
    Mono<Descriptor> findByExternalId(String externalId);
}
