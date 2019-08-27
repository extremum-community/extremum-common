package io.extremum.common.collection.service;

import io.extremum.common.descriptor.dao.DescriptorDao;
import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;

import java.util.Optional;

/**
 * @author rpuch
 */
public class CollectionDescriptorServiceImpl implements CollectionDescriptorService {
    private final DescriptorService descriptorService;
    private final DescriptorDao descriptorDao;

    public CollectionDescriptorServiceImpl(DescriptorService descriptorService, DescriptorDao descriptorDao) {
        this.descriptorService = descriptorService;
        this.descriptorDao = descriptorDao;
    }

    @Override
    public Optional<CollectionDescriptor> retrieveByExternalId(String externalId) {
        Optional<Descriptor> optDescriptor = descriptorService.loadByExternalId(externalId);

        optDescriptor.ifPresent(descriptor -> {
            makeSureDescriptorContainsCollection(externalId, descriptor);
        });

        return optDescriptor.map(Descriptor::getCollection);
    }

    private void makeSureDescriptorContainsCollection(String externalId, Descriptor descriptor) {
        if (descriptor.getType() != Descriptor.Type.COLLECTION) {
            throw new IllegalStateException(
                    String.format("Descriptor '%s' must have type COLLECTION, but it is '%s'",
                            externalId, descriptor.getType()));
        }
        if (descriptor.getCollection() == null) {
            throw new IllegalStateException(
                    String.format("Descriptor '%s' has type COLLECTION, but there is no collection in it",
                            externalId));
        }
    }

    @Override
    public Optional<Descriptor> retrieveByCoordinates(String coordinatesString) {
        return descriptorDao.retrieveByCollectionCoordinates(coordinatesString);
    }
}
