package io.extremum.common.collection.service;

import io.extremum.common.descriptor.dao.DescriptorDao;
import io.extremum.common.descriptor.factory.DescriptorSavers;
import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;
import org.springframework.dao.DuplicateKeyException;

import java.util.Optional;

/**
 * @author rpuch
 */
public class CollectionDescriptorServiceImpl implements CollectionDescriptorService {
    private final DescriptorService descriptorService;
    private final DescriptorDao descriptorDao;
    private final DescriptorSavers descriptorSavers;

    private final CollectionDescriptorVerifier collectionDescriptorVerifier = new CollectionDescriptorVerifier();

    public CollectionDescriptorServiceImpl(DescriptorService descriptorService, DescriptorDao descriptorDao) {
        this.descriptorService = descriptorService;
        this.descriptorDao = descriptorDao;
        descriptorSavers = new DescriptorSavers(descriptorService);
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

    @Override
    public Descriptor retrieveByCoordinatesOrCreate(CollectionDescriptor collectionDescriptor) {
        Descriptor descriptor = descriptorSavers.createCollectionDescriptor(collectionDescriptor);

        try {
            return descriptorDao.store(descriptor);
        } catch (DuplicateKeyException e) {
            return descriptorDao.retrieveByCollectionCoordinates(collectionDescriptor.toCoordinatesString())
                    .orElseThrow(() -> new IllegalStateException(cannotInsertNorFindMessage(collectionDescriptor)));
        }
    }

    private String cannotInsertNorFindMessage(CollectionDescriptor collectionDescriptor) {
        return String.format(
                "Could not save a collection descriptor with coordinates '%s', but it could not be found as well",
                collectionDescriptor.toCoordinatesString()
        );
    }
}
