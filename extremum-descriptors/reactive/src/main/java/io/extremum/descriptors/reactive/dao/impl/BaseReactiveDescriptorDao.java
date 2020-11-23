package io.extremum.descriptors.reactive.dao.impl;

import io.extremum.descriptors.reactive.dao.ReactiveDescriptorDao;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMapReactive;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.ReactiveMongoDatabaseUtils;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.transaction.reactive.TransactionSynchronization;
import org.springframework.transaction.reactive.TransactionSynchronizationManager;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;

import static org.springframework.data.mongodb.core.query.Criteria.where;


@Slf4j
public abstract class BaseReactiveDescriptorDao implements ReactiveDescriptorDao {
    private final RMapReactive<String, Descriptor> descriptors;
    private final RMapReactive<String, String> internalIdIndex;
    private final RMapReactive<String, String> collectionCoordinatesToExternalIds;
    private final ReactiveMongoOperations reactiveMongoOperations;
    private final ReactiveMongoDatabaseFactory mongoDatabaseFactory;

    BaseReactiveDescriptorDao(RMapReactive<String, Descriptor> descriptors,
                              RMapReactive<String, String> internalIdIndex,
                              RMapReactive<String, String> collectionCoordinatesToExternalIds,
                              ReactiveMongoOperations reactiveMongoOperations,
                              ReactiveMongoDatabaseFactory mongoDatabaseFactory) {
        this.descriptors = descriptors;
        this.internalIdIndex = internalIdIndex;
        this.collectionCoordinatesToExternalIds = collectionCoordinatesToExternalIds;
        this.reactiveMongoOperations = reactiveMongoOperations;
        this.mongoDatabaseFactory = mongoDatabaseFactory;
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
        return Mono.defer(() -> {
            if (log.isDebugEnabled()) {
                log.debug("Saving descriptor, externalId {}, readiness {}, version {}", descriptor.getExternalId(),
                        descriptor.getReadiness(), descriptor.getVersion());
            }
            return reactiveMongoOperations.save(descriptor)
                    .flatMap(this::putToMapsAfterWriteToMongoBecomesVisible);
        });
    }

    private Mono<Descriptor> putToMapsAfterWriteToMongoBecomesVisible(Descriptor savedToMongo) {
        return ReactiveMongoDatabaseUtils.isTransactionActive(mongoDatabaseFactory).flatMap(inTransaction -> {
            if (inTransaction) {
                if (log.isDebugEnabled()) {
                    log.debug("Will put to maps after transaction is committed {}", savedToMongo.getExternalId());
                }
                return putToMapsAfterTransactionCommit(savedToMongo);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Putting to maps outside of transaction {}, readiness {}, version {}",
                            savedToMongo.getExternalId(), savedToMongo.getReadiness(), savedToMongo.getVersion());
                }
                return putToMaps(savedToMongo).thenReturn(savedToMongo);
            }
        });
    }

    private Mono<? extends Descriptor> putToMapsAfterTransactionCommit(Descriptor savedToMongo) {
        return TransactionSynchronizationManager.forCurrentTransaction()
                .doOnNext(tsm -> tsm.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public Mono<Void> afterCommit() {
                        if (log.isDebugEnabled()) {
                            log.debug("Putting to maps after transaction commit {}, readiness {}, version {}",
                                    savedToMongo.getExternalId(), savedToMongo.getReadiness(),
                                    savedToMongo.getVersion());
                        }
                        return putToMaps(savedToMongo);
                    }
                }))
                .thenReturn(savedToMongo);
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

    @Override
    public Mono<Void> destroy(String externalId) {
        return descriptors.remove(externalId)
                .flatMap(removedDescriptor -> {
                    Mono<Void> doDeletions = Mono.empty();
                    if (removedDescriptor.hasInternalId()) {
                        doDeletions = doDeletions
                                .then(internalIdIndex.remove(removedDescriptor.getInternalId()))
                                .then();
                    }
                    if (removedDescriptor.isCollection()) {
                        String coordinatesString = removedDescriptor.getCollection().getCoordinatesString();
                        doDeletions = doDeletions
                                .then(collectionCoordinatesToExternalIds.remove(coordinatesString))
                                .then();
                    }
                    return doDeletions.then(deleteFromMongo(externalId));
                });
    }

    private Mono<Void> deleteFromMongo(String externalId) {
        return reactiveMongoOperations.remove(new Query(where("_id").is(externalId)), Descriptor.class)
                .then();
    }
}
