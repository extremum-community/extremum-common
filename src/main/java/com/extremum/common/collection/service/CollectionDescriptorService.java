package com.extremum.common.collection.service;

import com.extremum.common.collection.CollectionDescriptor;

import java.util.Optional;

/**
 * @author rpuch
 */
public interface CollectionDescriptorService {
    Optional<CollectionDescriptor> retrieveByCoordinates(String coordinatesString);

    void store(CollectionDescriptor descriptor);
}
