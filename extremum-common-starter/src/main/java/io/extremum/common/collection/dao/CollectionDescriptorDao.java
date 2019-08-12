package io.extremum.common.collection.dao;

import io.extremum.common.collection.CollectionDescriptor;

import java.util.Optional;

/**
 * @author rpuch
 */
public interface CollectionDescriptorDao {
    Optional<CollectionDescriptor> retrieveByExternalId(String externalId);

    Optional<CollectionDescriptor> retrieveByCoordinates(String coordinatesString);

    CollectionDescriptor store(CollectionDescriptor descriptor);
}
