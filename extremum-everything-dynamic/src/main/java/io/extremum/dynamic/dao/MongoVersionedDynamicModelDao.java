package io.extremum.dynamic.dao;

import com.mongodb.BasicDBObject;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.reactivestreams.client.Success;
import io.extremum.common.exceptions.ModelNotFoundException;
import io.extremum.common.model.VersionedModel;
import io.extremum.common.utils.DateUtils;
import io.extremum.dynamic.models.impl.JsonDynamicModel;
import io.extremum.mongo.MongoConstants;
import io.extremum.mongo.facilities.ReactiveMongoDescriptorFacilities;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.reactivestreams.Publisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import static reactor.core.publisher.Mono.*;

@RequiredArgsConstructor
public class MongoVersionedDynamicModelDao implements JsonDynamicModelDao {
    private static final long VERSION_INIT_VALUE = 1L;
    private final ReactiveMongoOperations operations;
    private final ReactiveMongoDescriptorFacilities descriptorFacilities;

    @Override
    public Mono<JsonDynamicModel> create(JsonDynamicModel model, String collectionName) {
        return makeDescriptor(model.getModelName())
                .flatMap(createServiceFields(model))
                .flatMap(enhanced -> from(operations.getCollection(collectionName)
                        .insertOne(new Document(model.getModelData())))
                        .then(just(model)));
    }

    private Function<Descriptor, Mono<JsonDynamicModel>> createServiceFields(JsonDynamicModel model) {
        return descr -> fromSupplier(() -> {
            ObjectId oId = new ObjectId(descr.getInternalId());
            ZonedDateTime now = ZonedDateTime.now();

            model.setId(descr);
            model.getModelData().put(Model.FIELDS.version.name(), VERSION_INIT_VALUE);
            model.getModelData().put(Model.FIELDS.created.name(), toDate(now));
            model.getModelData().put(Model.FIELDS.modified.name(), toDate(now));
            model.getModelData().put(Model.FIELDS.model.name(), model.getModelName());
            model.getModelData().put(VersionedModel.FIELDS.lineageId.name(), oId);
            model.getModelData().put(VersionedModel.FIELDS.currentSnapshot.name(), true);
            model.getModelData().put(VersionedModel.FIELDS.start.name(), toDate(now));
            model.getModelData().put(VersionedModel.FIELDS.end.name(), toDate(MongoConstants.DISTANT_FUTURE));

            return model;
        });
    }

    private Date toDate(ZonedDateTime zdt) {
        String tmp = DateUtils.formatZonedDateTimeISO_8601(zdt);
        return DateUtils.convert(tmp);
    }

    private Mono<Descriptor> makeDescriptor(String modelName) {
        return descriptorFacilities.create(new ObjectId(), modelName);
    }

    @Override
    @Transactional
    public Mono<JsonDynamicModel> replace(JsonDynamicModel updatedModel, String collectionName) {
        return getByIdFromCollection(updatedModel.getId(), collectionName)
                .flatMap(currentSnapshot -> {
                    Date now = toDate(ZonedDateTime.now());

                    Tuple2<JsonDynamicModel, JsonDynamicModel> prepared = prepareCurrentAndNextSnapshots(currentSnapshot, updatedModel, now);
                    JsonDynamicModel current = prepared.getT1();
                    JsonDynamicModel next = prepared.getT2();

                    List<Bson> andCriteria = new ArrayList<>();
                    andCriteria.add(new Document(VersionedModel.FIELDS.lineageId.name(), new ObjectId(next.getId().getInternalId())));
                    andCriteria.add(new Document(VersionedModel.FIELDS.currentSnapshot.name(), true));
                    andCriteria.add(new Document(Model.FIELDS.version.name(), updatedModel.getModelData().get(Model.FIELDS.version.name())));

                    Bson filter = new Document("$and", andCriteria);

                    List<Bson> update = new ArrayList<>();
                    update.add(new Document("$set", new Document(VersionedModel.FIELDS.currentSnapshot.name(), false)));
                    update.add(new Document("$set", new Document(VersionedModel.FIELDS.end.name(), now)));

                    Publisher<UpdateResult> p = operations.getCollection(collectionName)
                            .updateMany(filter, update);

                    return Flux.from(p)
                            .collectList()
                            .flatMap(results -> {
                                long updatedCount = results.stream()
                                        .mapToLong(UpdateResult::getMatchedCount)
                                        .sum();

                                if (updatedCount == 0) {
                                    return error(new OptimisticLockingFailureException("Model " + current.getId() + " changed"));
                                } else {
                                    Publisher<Success> insertPublisher = operations.getCollection(collectionName)
                                            .insertOne(new Document(next.getModelData()));

                                    return from(insertPublisher)
                                            .thenReturn(next);
                                }
                            });
                });
    }

    private Tuple2<JsonDynamicModel, JsonDynamicModel> prepareCurrentAndNextSnapshots(JsonDynamicModel currentSnapshot, JsonDynamicModel updatedModel, Date now) {
        JsonDynamicModel current = cloneModel(currentSnapshot);
        JsonDynamicModel next = cloneModel(updatedModel);

        current.getModelData().put(VersionedModel.FIELDS.end.name(), now);
        current.getModelData().put(VersionedModel.FIELDS.currentSnapshot.name(), false);

        next.getModelData().put(Model.FIELDS.version.name(), extractVersion(currentSnapshot) + 1);
        next.getModelData().put(VersionedModel.FIELDS.lineageId.name(), new ObjectId(currentSnapshot.getId().getInternalId()));
        next.getModelData().put(VersionedModel.FIELDS.currentSnapshot.name(), true);
        next.getModelData().put(Model.FIELDS.created.name(), now);
        next.getModelData().put(Model.FIELDS.modified.name(), now);
        next.getModelData().put(VersionedModel.FIELDS.start.name(), now);
        next.getModelData().put(VersionedModel.FIELDS.end.name(), toDate(MongoConstants.DISTANT_FUTURE));

        return Tuples.of(current, next);
    }

    private JsonDynamicModel cloneModel(JsonDynamicModel currentSnapshot) {
        return new JsonDynamicModel(currentSnapshot.getId(), currentSnapshot.getModelName(), new HashMap<>(currentSnapshot.getModelData()));
    }

    private long extractVersion(JsonDynamicModel model) {
        return (long) model.getModelData().get(Model.FIELDS.version.name());
    }

    @Override
    public Mono<JsonDynamicModel> getByIdFromCollection(Descriptor id, String collectionName) {
        return extractInternalId(id)
                .flatMap(getActiveSnapshotFromCollection(collectionName))
                .map(doc -> new JsonDynamicModel(id, doc.getString(Model.FIELDS.model.name()), doc))
                .switchIfEmpty(defer(() -> error(new ModelNotFoundException("Dynamic model with id " + id + " doesn't found"))));
    }

    private Function<ObjectId, Mono<Document>> getActiveSnapshotFromCollection(String collectionName) {
        return oId -> {
            List<BasicDBObject> criteria = new ArrayList<>();
            criteria.add(new BasicDBObject(VersionedModel.FIELDS.lineageId.name(), oId));
            criteria.add(new BasicDBObject(VersionedModel.FIELDS.currentSnapshot.name(), true));

            BasicDBObject condition = new BasicDBObject("$and", criteria);
            Publisher<Document> p = operations.getCollection(collectionName)
                    .find(condition).first();

            return from(p);
        };
    }

    @Override
    public Mono<Void> remove(Descriptor id, String collectionName) {
        return extractInternalId(id)
                .flatMap(oId -> {
                    List<BasicDBObject> update = new ArrayList<>();
                    update.add(new BasicDBObject("$set", new BasicDBObject(Model.FIELDS.deleted.name(), true)));
                    update.add(new BasicDBObject("$set", new BasicDBObject(VersionedModel.FIELDS.currentSnapshot.name(), false)));

                    Publisher<UpdateResult> p = operations.getCollection(collectionName)
                            .updateMany(
                                    new BasicDBObject(VersionedModel.FIELDS.lineageId.name(), oId),
                                    update
                            );

                    return Flux.from(p).collectList().then(empty());
                });
    }

    private Mono<ObjectId> extractInternalId(Descriptor id) {
        return id.getInternalIdReactively()
                .map(ObjectId::new);
    }
}
