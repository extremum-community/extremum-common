package io.extremum.mongo.dao.impl;

import io.extremum.common.exceptions.ModelNotFoundException;
import io.extremum.common.model.VersionedModel;
import io.extremum.mongo.MongoConstants;
import io.extremum.mongo.dao.ReactiveMongoVersionedDao;
import io.extremum.mongo.model.MongoVersionedModel;
import org.bson.types.ObjectId;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.ParameterizedType;
import java.time.ZonedDateTime;
import java.util.Objects;

import static org.springframework.data.mongodb.core.query.Criteria.where;

public abstract class ReactiveMongoVersionedDaoImpl<M extends MongoVersionedModel>
        implements ReactiveMongoVersionedDao<M> {
    private final ReactiveMongoOperations reactiveMongoOperations;
    private final Class<M> modelClass;

    private final VersionedQueries versionedQueries = new VersionedQueries();

    public ReactiveMongoVersionedDaoImpl(ReactiveMongoOperations reactiveMongoOperations) {
        this.reactiveMongoOperations = reactiveMongoOperations;
        modelClass = (Class<M>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    @Override
    public Flux<M> findAll() {
        return Flux.defer(() -> {
            Query query = queryWithActualSnapshotCriteria(new Query());
            return reactiveMongoOperations.find(query, modelClass);
        });
    }

    private Query queryWithActualSnapshotCriteria(Query originalQuery) {
        return originalQuery.addCriteria(versionedQueries.actualSnapshot());
    }

    @Override
    public Mono<M> findById(ObjectId lineageId) {
        return Mono.defer(() -> {
            Query query = queryByLineageId(lineageId);
            return reactiveMongoOperations.findOne(query, modelClass);
        });
    }

    private Query queryByLineageId(ObjectId lineageId) {
        Query query = new Query().addCriteria(where(VersionedModel.FIELDS.lineageId.name()).is(lineageId));
        query = queryWithActualSnapshotCriteria(query);
        return query;
    }

    @Override
    public Mono<Boolean> existsById(ObjectId lineageId) {
        return Mono.defer(() -> {
            Query query = queryByLineageId(lineageId);
            return reactiveMongoOperations.exists(query, modelClass);
        });
    }

    @Override
    public <N extends M> Mono<N> save(N model) {
        return Mono.defer(() -> {
            if (isNew(model)) {
                return addFirstSnapshot(model);
            } else {
                return findById(model.getLineageId())
                        .flatMap(currentSnapshot -> addNextSnapshot(model, currentSnapshot))
                        .switchIfEmpty(Mono.error(() -> modelNotFoundExceptionWhenUpdating(model)));
            }
        });
    }

    private <N extends M> ModelNotFoundException modelNotFoundExceptionWhenUpdating(N model) {
        String message = String.format("No current snapshot found by lineageId '%s' when trying to update",
                model.getLineageId());
        return new ModelNotFoundException(message);
    }

    private boolean isNew(M model) {
        return model.getLineageId() == null;
    }

    private <N extends M> Mono<? extends N> addFirstSnapshot(N model) {
        fillFirstSnapshot(model);
        return reactiveMongoOperations.insert(model);
    }

    private <N extends M> void fillFirstSnapshot(N newSnapshot) {
        ZonedDateTime now = ZonedDateTime.now();

        newSnapshot.setCreated(now);
        newSnapshot.setStart(now);
        newSnapshot.setEnd(infinitelyDistantFuture());
        newSnapshot.setCurrentSnapshot(true);
        newSnapshot.setVersion(0L);
    }

    private ZonedDateTime infinitelyDistantFuture() {
        return MongoConstants.DISTANT_FUTURE;
    }

    private <N extends M> Mono<N> addNextSnapshot(N newSnapshot, M currentSnapshot) {
        if (versionDiffers(newSnapshot, currentSnapshot)) {
            return Mono.error(versionDiffersOptimistickLockingException(newSnapshot, currentSnapshot));
        }

        prepareCurrentAndNextSnapshots(newSnapshot, currentSnapshot);

        return saveOldSnapshotAndInsertNewSnapshot(newSnapshot, currentSnapshot);
    }

    private <N extends M> boolean versionDiffers(N model, M currentSnapshot) {
        return !Objects.equals(currentSnapshot.getVersion(), model.getVersion());
    }

    private <N extends M> Exception versionDiffersOptimistickLockingException(N newSnapshot, M currentSnapshot) {
        return new OptimisticLockingFailureException(
                String.format("Trying to save a model with lineageId '%s' and version '%s' while it's already '%s'",
                        newSnapshot.getLineageId(), newSnapshot.getVersion(), currentSnapshot.getVersion()));
    }

    private <N extends M> void prepareCurrentAndNextSnapshots(N nextSnapshot, M currentSnapshot) {
        ZonedDateTime now = ZonedDateTime.now();

        currentSnapshot.setEnd(now);
        currentSnapshot.setCurrentSnapshot(false);

        nextSnapshot.setUuid(currentSnapshot.getUuid());
        nextSnapshot.setSnapshotId(newSnapshotId());
        nextSnapshot.setStart(now);
        nextSnapshot.setEnd(infinitelyDistantFuture());
        nextSnapshot.setCurrentSnapshot(true);
        nextSnapshot.setVersion(nextSnapshot.getVersion() + 1);
    }

    private ObjectId newSnapshotId() {
        return new ObjectId();
    }

    private <N extends M> Mono<N> saveOldSnapshotAndInsertNewSnapshot(N nextSnapshot, M currentSnapshot) {
        return reactiveMongoOperations.inTransaction().execute(sessionBound -> {
            return sessionBound.save(currentSnapshot)
                    .then(sessionBound.insert(nextSnapshot));
        }).then(Mono.just(nextSnapshot));
    }

    @Override
    public <N extends M> Flux<N> saveAll(Iterable<N> entities) {
        return Flux.fromIterable(entities)
                .concatMap(this::save);
    }

    @Override
    public Mono<Void> deleteById(ObjectId lineageId) {
        return deleteByIdAndReturn(lineageId).then();
    }

    @Override
    public Mono<M> deleteByIdAndReturn(ObjectId lineageId) {
        return findById(lineageId).flatMap(found -> {
            found.setDeleted(true);
            return save(found);
        }).switchIfEmpty(Mono.error(new ModelNotFoundException(modelClass, lineageId.toString())));
    }
}