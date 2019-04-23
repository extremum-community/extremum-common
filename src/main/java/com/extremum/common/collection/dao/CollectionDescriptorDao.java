package com.extremum.common.collection.dao;

import com.extremum.common.collection.CollectionDescriptor;

import java.util.Optional;

/**
 * @author rpuch
 */
public interface CollectionDescriptorDao {
    Optional<CollectionDescriptor> retrieveByExternalId(String externalId);

    CollectionDescriptor store(CollectionDescriptor descriptor);
}
