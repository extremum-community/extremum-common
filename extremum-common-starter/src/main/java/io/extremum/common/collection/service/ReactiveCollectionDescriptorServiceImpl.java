package io.extremum.common.collection.service;

import io.extremum.common.descriptor.dao.ReactiveDescriptorDao;
import io.extremum.common.descriptor.factory.DescriptorSavers;
import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;
import org.springframework.dao.DuplicateKeyException;
import reactor.core.publisher.Mono;

public class ReactiveCollectionDescriptorServiceImpl implements ReactiveCollectionDescriptorService {
    private final ReactiveDescriptorDao reactiveDescriptorDao;
    private final DescriptorSavers descriptorSavers;

    private final CollectionDescriptorVerifier collectionDescriptorVerifier = new CollectionDescriptorVerifier();

    public ReactiveCollectionDescriptorServiceImpl(ReactiveDescriptorDao reactiveDescriptorDao,
                                                   DescriptorService descriptorService) {
        this.reactiveDescriptorDao = reactiveDescriptorDao;
        this.descriptorSavers = new DescriptorSavers(descriptorService);
    }

    @Override
    public Mono<CollectionDescriptor> retrieveByExternalId(String externalId) {
        return reactiveDescriptorDao.retrieveByExternalId(externalId)
                .map(descriptor -> {
                    collectionDescriptorVerifier.makeSureDescriptorContainsCollection(externalId, descriptor);
                    return descriptor;
                })
                .map(Descriptor::getCollection);
    }

    @Override
    public Mono<Descriptor> retrieveByCoordinates(String coordinatesString) {
        return reactiveDescriptorDao.retrieveByCollectionCoordinates(coordinatesString)
                .map(descriptor -> {
                    collectionDescriptorVerifier.makeSureDescriptorContainsCollection(
                            descriptor.getExternalId(), descriptor);
                    return descriptor;
                });
    }

    @Override
    public Mono<Descriptor> retrieveByCoordinatesOrCreate(CollectionDescriptor collectionDescriptor) {
        Descriptor descriptor = descriptorSavers.createCollectionDescriptor(collectionDescriptor);
        return reactiveDescriptorDao.store(descriptor)
                .onErrorResume(DuplicateKeyException.class,
                        e -> retrieveByCoordinates(collectionDescriptor.toCoordinatesString()));
    }
}
