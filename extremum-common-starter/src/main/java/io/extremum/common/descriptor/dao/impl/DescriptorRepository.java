package io.extremum.common.descriptor.dao.impl;

import io.extremum.sharedmodels.descriptor.Descriptor;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author rpuch
 */
@Repository
public interface DescriptorRepository extends MongoRepository<Descriptor, String> {
    Optional<Descriptor> findByExternalId(String externalId);

    Optional<Descriptor> findByInternalId(String internalId);

    Optional<Descriptor> findByCollectionCoordinatesString(String coordinatesString);
}
