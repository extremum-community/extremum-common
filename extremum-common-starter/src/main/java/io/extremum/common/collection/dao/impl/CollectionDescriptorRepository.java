package io.extremum.common.collection.dao.impl;

import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author rpuch
 */
@Repository
public interface CollectionDescriptorRepository extends MongoRepository<CollectionDescriptor, String> {
    Optional<CollectionDescriptor> findByExternalId(String externalId);

    Optional<CollectionDescriptor> findByCoordinatesString(String internalId);
}
