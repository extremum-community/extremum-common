package io.extremum.dynamic.dao;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.reactivestreams.client.FindPublisher;
import io.extremum.common.exceptions.CommonException;
import io.extremum.common.utils.DateUtils;
import io.extremum.dynamic.models.impl.BsonDynamicModel;
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

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.function.Function;

import static io.extremum.sharedmodels.basic.Model.FIELDS.*;
import static java.lang.String.format;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static reactor.core.publisher.Mono.*;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MongoBsonDynamicModelDao implements DynamicModelDao<BsonDynamicModel> {
    private static final long INITIAL_VERSION_VALUE = 1L;

    private final ReactiveMongoOperations mongoOperations;
    private final ReactiveMongoDescriptorFacilities mongoDescriptorFacilities;

    @Override
    public Mono<BsonDynamicModel> create(BsonDynamicModel model, String collectionName) {
        return justOrEmpty(model.getId())
                .switchIfEmpty(createNewDescriptor(model))
                .map(descriptor -> {
                            Document doc = model.getModelData()
                                    .append("_id", new ObjectId(descriptor.getInternalId()));
                            provideServiceFields(doc, model);
                            return Tuples.of(doc, descriptor);
                        }
                ).flatMap(tuple2 -> mongoOperations.save(tuple2.getT1(), collectionName)
                        .doOnNext(successPublisher ->
                                log.info("Document {} saved", tuple2.getT2().getInternalId()))
                        .map(_it -> new BsonDynamicModel(tuple2.getT2(), model.getModelName(), tuple2.getT1()))
                );
    }

    private void provideServiceFields(Document doc, BsonDynamicModel model) {
        String now = getNowDateAsString();
        doc.append(created.name(), now);
        doc.append(modified.name(), now);
        doc.append(version.name(), INITIAL_VERSION_VALUE);
        doc.append(Model.FIELDS.model.name(), model.getModelName());
    }

    @Override
    public Mono<BsonDynamicModel> replace(BsonDynamicModel model, String collectionName) {
        Objects.requireNonNull(model.getId(), "ID of a model can't be null");

        return just(model.getId())
                .flatMap(Descriptor::getInternalIdReactively)
                .flatMap(doUpdate(model, collectionName));
    }

    protected Function<String, Mono<BsonDynamicModel>> doUpdate(BsonDynamicModel model, String collectionName) {
        return modelId -> {
            ObjectId modelObjectId = new ObjectId(modelId);

            Document doc = model.getModelData()
                    .append("_id", modelObjectId);

            validateServiceFields(doc, model);

            Long oldDocVersion = extractVersion(doc);

            updateServiceFields(doc);

            Query query = Query.query(
                    where("_id").is(modelObjectId)
                            .andOperator(where(version.name())
                                    .is(oldDocVersion)
                            ));

            String msg = format("Unable to update document %s", model.getId());

            return mongoOperations.findAndReplace(query, doc, collectionName)
                    .doOnNext(updatedDoc -> log.info("Document {} updated", model.getId()))
                    .map(_it -> new BsonDynamicModel(model.getId(), model.getModelName(), doc))
                    .switchIfEmpty(error(new OptimisticLockingFailureException(msg)));
        };
    }

    private void validateServiceFields(Document doc, BsonDynamicModel mo) {
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

    private void updateServiceFields(Document doc) {
        doc.replace(modified.name(), getNowDateAsString());
        doc.replace(version.name(), extractVersion(doc) + 1);
    }

    private Long extractVersion(Document doc) {
        return Long.valueOf(doc.get(version.name()).toString());
    }

    private String getNowDateAsString() {
        return DateUtils.formatZonedDateTimeISO_8601(ZonedDateTime.now());
    }

    @Override
    public Mono<BsonDynamicModel> getByIdFromCollection(Descriptor id, String collectionName) {
        FindPublisher<Document> p = mongoOperations.getCollection(collectionName)
                .find(new Document("_id", new ObjectId(id.getInternalId())));

        return from(p)
                .flatMap(doc ->
                        mongoDescriptorFacilities
                                .fromInternalId(doc.getObjectId("_id"))
                                .map(descr -> {
                                    doc.remove("_id");
                                    return new BsonDynamicModel(descr, descr.getModelType(), doc);
                                })
                );
    }

    @Override
    public Mono<Void> remove(Descriptor id, String collectionName) {
        Publisher<DeleteResult> deleteResultPublisher = mongoOperations.getCollection(collectionName)
                .deleteOne(new Document("_id", new ObjectId(id.getInternalId())));

        return Mono.from(deleteResultPublisher).then();
    }

    private Mono<Descriptor> createNewDescriptor(BsonDynamicModel model) {
        return mongoDescriptorFacilities.create(ObjectId.get(), model.getModelName());
    }
}
