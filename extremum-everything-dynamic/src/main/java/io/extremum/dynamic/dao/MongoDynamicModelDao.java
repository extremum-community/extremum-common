package io.extremum.dynamic.dao;

import com.mongodb.client.result.UpdateResult;
import com.mongodb.reactivestreams.client.FindPublisher;
import io.extremum.common.exceptions.CommonException;
import io.extremum.dynamic.models.impl.JsonDynamicModel;
import io.extremum.mongo.facilities.ReactiveMongoDescriptorFacilities;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.constant.HttpStatus;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.reactivestreams.Publisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.set;
import static io.extremum.sharedmodels.basic.Model.FIELDS.*;
import static java.lang.String.format;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static reactor.core.publisher.Mono.*;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MongoDynamicModelDao implements DynamicModelDao<JsonDynamicModel> {
    private static final long INITIAL_VERSION_VALUE = 1L;

    private final ReactiveMongoOperations mongoOperations;
    private final ReactiveMongoDescriptorFacilities mongoDescriptorFacilities;

    @Override
    public Mono<JsonDynamicModel> create(JsonDynamicModel model, String collectionName) {
        return justOrEmpty(model.getId())
                .switchIfEmpty(createNewDescriptor(model))
                .map(descriptor -> {
                            Map<String, Object> modelData = model.getModelData();

                            modelData.put("_id", new ObjectId(descriptor.getInternalId()));
                            provideServiceFields(modelData, model);
                            return Tuples.of(modelData, descriptor);
                        }
                ).flatMap(tuple2 -> mongoOperations.save(tuple2.getT1(), collectionName)
                        .doOnNext(successPublisher ->
                                log.info("Document {} saved", tuple2.getT2().getInternalId()))
                        .map(_it -> new JsonDynamicModel(tuple2.getT2(), model.getModelName(), tuple2.getT1()))
                );
    }

    private void provideServiceFields(Map<String, Object> doc, JsonDynamicModel model) {
        Date now = getNowDate();
        doc.put(created.name(), now);
        doc.put(modified.name(), now);
        doc.put(version.name(), INITIAL_VERSION_VALUE);
        doc.put(Model.FIELDS.model.name(), model.getModelName());
    }

    @Override
    public Mono<JsonDynamicModel> replace(JsonDynamicModel model, String collectionName) {
        Objects.requireNonNull(model.getId(), "ID of a model can't be null");

        return just(model.getId())
                .flatMap(Descriptor::getInternalIdReactively)
                .flatMap(doUpdate(model, collectionName));
    }

    protected Function<String, Mono<JsonDynamicModel>> doUpdate(JsonDynamicModel model, String collectionName) {
        return modelId -> {
            ObjectId modelObjectId = new ObjectId(modelId);

            Map<String, Object> modelData = model.getModelData();

            validateServiceFields(modelData, model);

            Long oldDocVersion = extractVersion(modelData);

            updateServiceFields(modelData);

            Query query = Query.query(
                    where("_id").is(modelObjectId)
                            .andOperator(where(version.name())
                                    .is(oldDocVersion)
                            ));

            String msg = format("Unable to update document %s", model.getId());

            return mongoOperations.findAndReplace(query, modelData, collectionName)
                    .doOnNext(updatedDoc -> log.info("Document {} updated", model.getId()))
                    .map(_it -> new JsonDynamicModel(model.getId(), model.getModelName(), modelData))
                    .switchIfEmpty(error(new OptimisticLockingFailureException(msg)));
        };
    }

    private void validateServiceFields(Map<String, Object> doc, JsonDynamicModel mo) {
        if (!doc.containsKey(created.name())) {
            throw new CommonException(format("Field %s is not presented", created.name()), HttpStatus.BAD_REQUEST.value());
        }

        if (!doc.containsKey(modified.name())) {
            throw new CommonException(format("Field %s is not presented", modified.name()), HttpStatus.BAD_REQUEST.value());
        }

        if (!doc.containsKey(version.name())) {
            throw new CommonException(format("Field %s is not presented", version.name()), HttpStatus.BAD_REQUEST.value());
        }

        if (!(doc.get(version.name()) instanceof Number)) {
            throw new CommonException(format("Invalid format for field %s. %s must be type %s but %s passed",
                    version.name(), version.name(), Number.class, doc.get(version.name()).getClass()), HttpStatus.BAD_REQUEST.value());
        }

        if (!doc.containsKey(model.name())) {
            throw new CommonException(format("Field %s is not presented", model.name()), HttpStatus.BAD_REQUEST.value());
        }

        if (!doc.get(model.name()).equals(mo.getModelName())) {
            throw new CommonException(format("Model name from update %s does not equal with %s",
                    doc.get(model.name()), mo.getModelName()), HttpStatus.BAD_REQUEST.value());
        }
    }

    private void updateServiceFields(Map<String, Object> doc) {
        doc.replace(modified.name(), getNowDate());
        doc.replace(version.name(), extractVersion(doc) + 1);
    }

    private Long extractVersion(Map<String, Object> doc) {
        return Long.valueOf(doc.get(version.name()).toString());
    }

    private Date getNowDate() {
        return new Date();
    }

    @Override
    public Mono<JsonDynamicModel> getByIdFromCollection(Descriptor id, String collectionName) {
        FindPublisher<Document> p = mongoOperations.getCollection(collectionName)
                .find(
                        and(
                                eq("_id", new ObjectId(id.getInternalId())),
                                or(
                                        eq(deleted.name(), false),
                                        exists(deleted.name(), false)
                                )
                        )
                );

        return from(p)
                .flatMap(doc ->
                        mongoDescriptorFacilities
                                .fromInternalId(doc.getObjectId("_id"))
                                .map(descr -> {
                                    doc.remove("_id");
                                    return new JsonDynamicModel(descr, descr.getModelType(), doc);
                                })
                );
    }

    @Override
    public Mono<Void> remove(Descriptor id, String collectionName) {
        Publisher<UpdateResult> result = mongoOperations.getCollection(collectionName)
                .updateOne(
                        and(
                                eq("_id", new ObjectId(id.getInternalId())),
                                or(
                                        eq(deleted.name(), false),
                                        exists(deleted.name(), false)
                                )
                        ),
                        set(deleted.name(), true)
                );

        return Mono.from(result).then();
    }

    private Mono<Descriptor> createNewDescriptor(JsonDynamicModel model) {
        return mongoDescriptorFacilities.create(ObjectId.get(), model.getModelName());
    }
}
