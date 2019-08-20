package io.extremum.common.collection.dao.impl;

import io.extremum.common.collection.CollectionDescriptor;
import io.extremum.common.collection.dao.ReactiveCollectionDescriptorDao;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMapReactive;
import reactor.core.publisher.Mono;


@Slf4j
public abstract class BaseReactiveCollectionDescriptorDao implements ReactiveCollectionDescriptorDao {
    private final RMapReactive<String, CollectionDescriptor> descriptors;

    BaseReactiveCollectionDescriptorDao(RMapReactive<String, CollectionDescriptor> descriptors) {
        this.descriptors = descriptors;
    }

    @Override
    public Mono<CollectionDescriptor> retrieveByExternalId(String externalId) {
        return descriptors.get(externalId);
    }
}
