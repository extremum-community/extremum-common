package io.extremum.common.descriptor.dao.impl;

import io.extremum.common.descriptor.dao.ReactiveDescriptorDao;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMapReactive;
import reactor.core.publisher.Mono;


@Slf4j
public abstract class BaseReactiveDescriptorDao implements ReactiveDescriptorDao {
    private final RMapReactive<String, Descriptor> descriptors;
    private final RMapReactive<String, String> internalIdIndex;

    BaseReactiveDescriptorDao(RMapReactive<String, Descriptor> descriptors,
                              RMapReactive<String, String> internalIdIndex) {
        this.descriptors = descriptors;
        this.internalIdIndex = internalIdIndex;
    }

    @Override
    public Mono<Descriptor> retrieveByExternalId(String externalId) {
        return descriptors.get(externalId);
    }

    @Override
    public Mono<Descriptor> retrieveByInternalId(String internalId) {
        return internalIdIndex.get(internalId).flatMap(descriptors::get);
    }

}
