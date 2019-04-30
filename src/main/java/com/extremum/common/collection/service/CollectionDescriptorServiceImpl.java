package com.extremum.common.collection.service;

import com.extremum.common.collection.CollectionDescriptor;
import com.extremum.common.collection.dao.CollectionDescriptorDao;

import java.util.Optional;

/**
 * @author rpuch
 */
public class CollectionDescriptorServiceImpl implements CollectionDescriptorService {
    private final CollectionDescriptorDao collectionDescriptorDao;

    public CollectionDescriptorServiceImpl(CollectionDescriptorDao collectionDescriptorDao) {
        this.collectionDescriptorDao = collectionDescriptorDao;
    }

    @Override
    public Optional<CollectionDescriptor> retrieveByExternalId(String externalId) {
        return collectionDescriptorDao.retrieveByExternalId(externalId);
    }

    @Override
    public Optional<CollectionDescriptor> retrieveByCoordinates(String coordinatesString) {
        return collectionDescriptorDao.retrieveByCoordinates(coordinatesString);
    }

    @Override
    public void store(CollectionDescriptor descriptor) {
        collectionDescriptorDao.store(descriptor);
    }
}
