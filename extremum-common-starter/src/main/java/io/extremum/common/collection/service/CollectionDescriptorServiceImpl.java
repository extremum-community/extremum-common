package io.extremum.common.collection.service;

import io.extremum.common.collection.dao.CollectionDescriptorDao;
import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;

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
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Optional<Descriptor> retrieveByCoordinates(String coordinatesString) {
        throw new UnsupportedOperationException("Not implemented yet");
//        return collectionDescriptorDao.retrieveByCoordinates(coordinatesString);
    }
}
