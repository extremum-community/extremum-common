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

    private final CollectionDescriptorVerifier collectionDescriptorVerifier = new CollectionDescriptorVerifier();

    public CollectionDescriptorServiceImpl(DescriptorService descriptorService, DescriptorDao descriptorDao) {
        this.descriptorService = descriptorService;
        this.descriptorDao = descriptorDao;
    }

    @Override
    public Optional<CollectionDescriptor> retrieveByExternalId(String externalId) {
        Optional<Descriptor> optDescriptor = descriptorService.loadByExternalId(externalId);

        optDescriptor.ifPresent(descriptor -> {
            collectionDescriptorVerifier.makeSureDescriptorContainsCollection(externalId, descriptor);
        });

        return optDescriptor.map(Descriptor::getCollection);
    }

    @Override
    public Optional<Descriptor> retrieveByCoordinates(String coordinatesString) {
        return descriptorDao.retrieveByCollectionCoordinates(coordinatesString);
    }
}
