package io.extremum.common.descriptor.dao.impl;

import io.extremum.common.descriptor.dao.ReactiveDescriptorDao;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMapReactive;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;


@Slf4j
public abstract class BaseReactiveDescriptorDao implements ReactiveDescriptorDao {
    private final RMapReactive<String, Descriptor> descriptors;
    private final RMapReactive<String, String> internalIdIndex;
    private final RMapReactive<String, String> collectionCoordinatesToExternalIds;
    private final ReactiveMongoOperations reactiveMongoOperations;

    private final DescriptorInitializations initializations = new DescriptorInitializations();

    BaseReactiveDescriptorDao(RMapReactive<String, Descriptor> descriptors,
                              RMapReactive<String, String> internalIdIndex,
                              RMapReactive<String, String> collectionCoordinatesToExternalIds,
                              ReactiveMongoOperations reactiveMongoOperations) {
        this.descriptors = descriptors;
        this.internalIdIndex = internalIdIndex;
        this.collectionCoordinatesToExternalIds = collectionCoordinatesToExternalIds;
        this.reactiveMongoOperations = reactiveMongoOperations;
    }

    @Override
    public Mono<Descriptor> retrieveByExternalId(String externalId) {
        return descriptors.get(externalId);
    }

    @Override
    public Mono<Descriptor> retrieveByInternalId(String internalId) {
        return internalIdIndex.get(internalId)
                .flatMap(descriptors::get);
    }

    @Override
    public Mono<Descriptor> retrieveByCollectionCoordinates(String collectionCoordinates) {
        return collectionCoordinatesToExternalIds.get(collectionCoordinates)
                .flatMap(descriptors::get);
    }

    @Override
    public Mono<Descriptor> store(Descriptor descriptor) {
        initializations.fillCreatedAndModifiedDatesManuallyToHaveFullyFilledObjectInRedis(descriptor);

        boolean initializeVersionManually = initializations.shouldInitializeVersionManually(descriptor);

        initializations.fillVersionIfNeededToHaveFullyFilledObjectInRedis(descriptor, initializeVersionManually);

        return putToMaps(descriptor)
                .then(Mono.defer(() -> {
                    initializations.removeVersionIfItWasSetManually(descriptor, initializeVersionManually);
                    return reactiveMongoOperations.save(descriptor);
                }));
    }

    private Mono<Void> putToMaps(Descriptor descriptor) {
        Mono<Void> afterPutToDescriptors = descriptors.put(descriptor.getExternalId(), descriptor).then();
        if (descriptor.isSingle()) {
            return afterPutToDescriptors.then(
                    internalIdIndex.put(descriptor.getInternalId(), descriptor.getExternalId())
            ).then();
        } else if (descriptor.isCollection()) {
            return afterPutToDescriptors.then(
                    collectionCoordinatesToExternalIds.put(descriptor.getCollection().toCoordinatesString(),
                            descriptor.getExternalId())
            ).then();
        }

        return afterPutToDescriptors;
    }

    @Override
    public Mono<Map<String, String>> retrieveMapByInternalIds(Collection<String> internalIds) {
        Objects.requireNonNull(internalIds, "internalIds is null");

        return internalIdIndex.getAll(new HashSet<>(internalIds));
    }
}
