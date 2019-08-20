package io.extremum.common.collection.service;

import io.extremum.common.collection.CollectionDescriptor;
import io.extremum.common.collection.dao.ReactiveCollectionDescriptorDao;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ReactiveCollectionDescriptorServiceImpl implements ReactiveCollectionDescriptorService {
    private final ReactiveCollectionDescriptorDao reactiveCollectionDescriptorDao;

    @Override
    public Mono<CollectionDescriptor> retrieveByExternalId(String externalId) {
        return reactiveCollectionDescriptorDao.retrieveByExternalId(externalId);
    }
}
