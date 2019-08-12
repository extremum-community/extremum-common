package io.extremum.common.collection.service;

import io.extremum.common.collection.CollectionDescriptor;

import java.util.Optional;

/**
 * @author rpuch
 */
public interface CollectionDescriptorService {
    Optional<CollectionDescriptor> retrieveByExternalId(String externalId);

    Optional<CollectionDescriptor> retrieveByCoordinates(String coordinatesString);

    void store(CollectionDescriptor descriptor);
}
