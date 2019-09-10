package io.extremum.common.descriptor.dao.impl;

import io.extremum.common.descriptor.dao.ReactiveDescriptorDao;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMapReactive;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import reactor.core.publisher.Mono;


@Slf4j
public abstract class BaseReactiveDescriptorDao implements ReactiveDescriptorDao {
    private final RMapReactive<String, Descriptor> descriptors;
    private final RMapReactive<String, String> internalIdIndex;
    private final ReactiveMongoOperations reactiveMongoOperations;

    BaseReactiveDescriptorDao(RMapReactive<String, Descriptor> descriptors,
                              RMapReactive<String, String> internalIdIndex,
                              ReactiveMongoOperations reactiveMongoOperations) {
        this.descriptors = descriptors;
        this.internalIdIndex = internalIdIndex;
        this.reactiveMongoOperations = reactiveMongoOperations;
    }

    @Override
    public Mono<Descriptor> retrieveByExternalId(String externalId) {
        return descriptors.get(externalId);
    }

    @Override
    public Mono<Descriptor> retrieveByInternalId(String internalId) {
        return internalIdIndex.get(internalId).flatMap(descriptors::get);
    }

    @Override
    public Mono<Descriptor> store(Descriptor descriptor) {
        return reactiveMongoOperations.save(descriptor)
                .flatMap(savedDescriptor -> putToMaps(savedDescriptor)
                        .thenReturn(savedDescriptor));
    }

    private Mono<Void> putToMaps(Descriptor descriptor) {
        Mono<Void> afterPutToDescriptors = descriptors.put(descriptor.getExternalId(), descriptor).then();
        if (descriptor.isSingle()) {
            return afterPutToDescriptors.then(
                    internalIdIndex.put(descriptor.getInternalId(), descriptor.getExternalId())
            ).then();
        }

        return afterPutToDescriptors;
    }
}
