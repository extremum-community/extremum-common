package io.extremum.common.collection.service;

import io.extremum.common.descriptor.dao.ReactiveDescriptorDao;
import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ReactiveCollectionDescriptorServiceImpl implements ReactiveCollectionDescriptorService {
    private final ReactiveDescriptorDao reactiveDescriptorDao;

    private final CollectionDescriptorVerifier collectionDescriptorVerifier = new CollectionDescriptorVerifier();

    @Override
    public Mono<CollectionDescriptor> retrieveByExternalId(String externalId) {
        return reactiveDescriptorDao.retrieveByExternalId(externalId)
                .map(descriptor -> {
                    collectionDescriptorVerifier.makeSureDescriptorContainsCollection(externalId, descriptor);
                    return descriptor;
                })
                .map(Descriptor::getCollection);
    }
}
