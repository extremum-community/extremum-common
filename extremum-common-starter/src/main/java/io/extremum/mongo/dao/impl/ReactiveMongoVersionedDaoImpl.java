package io.extremum.mongo.dao.impl;

import io.extremum.common.exceptions.ModelNotFoundException;
import io.extremum.mongo.MongoConstants;
import io.extremum.mongo.SoftDeletion;
import io.extremum.common.model.VersionedModel;
import io.extremum.mongo.dao.ReactiveMongoVersionedDao;
import io.extremum.mongo.model.MongoVersionedModel;
import org.bson.types.ObjectId;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.ParameterizedType;
import java.time.ZonedDateTime;
import java.util.Objects;

import static org.springframework.data.mongodb.core.query.Criteria.where;

public abstract class ReactiveMongoVersionedDaoImpl<M extends MongoVersionedModel>
        implements ReactiveMongoVersionedDao<M> {
    private final ReactiveMongoOperations mongoOperations;
    private final Class<M> modelClass;

    private final SoftDeletion softDeletion = new SoftDeletion();

    public ReactiveMongoVersionedDaoImpl(ReactiveMongoOperations mongoOperations) {
        this.mongoOperations = mongoOperations;
        modelClass = (Class<M>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    @Override
    public Flux<M> findAll() {
        return Flux.defer(() -> {
            Query query = queryWithActualSnapshotCriteria(new Query());
            return mongoOperations.find(query, modelClass);
        });
    }

    private Query queryWithActualSnapshotCriteria(Query originalQuery) {
        return originalQuery.addCriteria(actualSnapshot());
    }

    private Criteria actualSnapshot() {
        ZonedDateTime now = ZonedDateTime.now();
        return new Criteria().andOperator(
                softDeletion.notDeleted(),
                where(VersionedModel.FIELDS.start.name()).lte(now),
                where(VersionedModel.FIELDS.end.name()).gt(now)
        );
    }

    @Override
    public Mono<M> findById(ObjectId historyId) {
        return Mono.defer(() -> {
            Query query = queryByHistoryId(historyId);
            return mongoOperations.findOne(query, modelClass);
        });
    }

    private Query queryByHistoryId(ObjectId historyId) {
        Query query = new Query().addCriteria(where(VersionedModel.FIELDS.historyId.name()).is(historyId));
        query = queryWithActualSnapshotCriteria(query);
        return query;
    }

    @Override
    public Mono<Boolean> existsById(ObjectId historyId) {
        return Mono.defer(() -> {
            Query query = queryByHistoryId(historyId);
            return mongoOperations.exists(query, modelClass);
        });
    }

    @Override
    public <N extends M> Mono<N> save(N model) {
        return Mono.defer(() -> {
            if (isNew(model)) {
                return addFirstSnapshot(model);
            } else {
                return findById(model.getHistoryId())
                        .flatMap(currentSnapshot -> addNextSnapshot(model, currentSnapshot));
            }
        });
    }

    private boolean isNew(M model) {
        return model.getHistoryId() == null;
    }

    private <N extends M> Mono<? extends N> addFirstSnapshot(N model) {
        fillFirstSnapshot(model);
        return mongoOperations.insert(model);
    }

    private <N extends M> void fillFirstSnapshot(N newSnapshot) {
        ZonedDateTime now = ZonedDateTime.now();

        newSnapshot.setHistoryId(newHistoryId());
        newSnapshot.setCreated(now);
        newSnapshot.setStart(now);
        newSnapshot.setEnd(infinitelyDistantFuture());
        newSnapshot.setVersion(0L);
    }

    private ObjectId newHistoryId() {
        return new ObjectId();
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
                String.format("Trying to save a model with historyId '%s' and version '%s' while it's already '%s'",
                        newSnapshot.getHistoryId(), newSnapshot.getVersion(), currentSnapshot.getVersion()));
    }

    private <N extends M> void prepareCurrentAndNextSnapshots(N nextSnapshot, M currentSnapshot) {
        ZonedDateTime now = ZonedDateTime.now();

        currentSnapshot.setEnd(now);

        nextSnapshot.setSnapshotId(newSnapshotId());
        nextSnapshot.setStart(now);
        nextSnapshot.setEnd(infinitelyDistantFuture());
        nextSnapshot.setVersion(nextSnapshot.getVersion() + 1);
    }

    private ObjectId newSnapshotId() {
        return new ObjectId();
    }

    private <N extends M> Mono<N> saveOldSnapshotAndInsertNewSnapshot(N nextSnapshot, M currentSnapshot) {
        return mongoOperations.inTransaction().execute(sessionBound -> {
            return sessionBound.save(currentSnapshot)
                    .then(sessionBound.insert(nextSnapshot));
        }).then(Mono.just(nextSnapshot));
    }

    @Override
    public <N extends M> Flux<N> saveAll(Iterable<N> entities) {
        return Flux.fromIterable(entities)
                .flatMap(this::save);
    }

    @Override
    public Mono<Void> deleteById(ObjectId historyId) {
        return deleteByIdAndReturn(historyId).then();
    }

    @Override
    public Mono<M> deleteByIdAndReturn(ObjectId historyId) {
        return findById(historyId).flatMap(found -> {
            found.setDeleted(true);
            return save(found);
        }).switchIfEmpty(Mono.error(new ModelNotFoundException(modelClass, historyId.toString())));
    }
}
