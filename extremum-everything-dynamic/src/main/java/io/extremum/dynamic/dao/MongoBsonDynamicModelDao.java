package io.extremum.dynamic.dao;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.reactivestreams.client.FindPublisher;
import io.extremum.dynamic.models.impl.BsonDynamicModel;
import io.extremum.mongo.facilities.ReactiveMongoDescriptorFacilities;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.reactivestreams.Publisher;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.util.Objects;

import static reactor.core.publisher.Mono.*;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MongoBsonDynamicModelDao implements DynamicModelDao<BsonDynamicModel> {
    private final ReactiveMongoOperations mongoOperations;
    private final ReactiveMongoDescriptorFacilities mongoDescriptorFacilities;

    @Override
    public Mono<BsonDynamicModel> create(BsonDynamicModel model, String collectionName) {
        return justOrEmpty(model.getId())
                .switchIfEmpty(createNewDescriptor(model))
                .map(descriptor -> {
                            Document doc = model.getModelData()
                                    .append("_id", new ObjectId(descriptor.getInternalId()));
                            return Tuples.of(doc, descriptor);
                        }
                ).flatMap(tuple2 -> mongoOperations.save(tuple2.getT1(), collectionName)
                        .doOnNext(successPublisher ->
                                log.info("Document {} saved", tuple2.getT2().getInternalId()))
                        .map(_it -> new BsonDynamicModel(tuple2.getT2(), model.getModelName(), model.getModelData()))
                );
    }

    @Override
    public Mono<BsonDynamicModel> replace(BsonDynamicModel model, String collectionName) {
        Objects.requireNonNull(model.getId(), "ID of a model can't be null");

        return just(model.getId())
                .map(descriptor -> {
                            Document doc = model.getModelData()
                                    .append("_id", new ObjectId(descriptor.getInternalId()));
                            return Tuples.of(doc, descriptor);
                        }
                ).flatMap(tuple2 -> mongoOperations.save(tuple2.getT1(), collectionName)
                        .doOnNext(updatePublisher ->
                                log.info("Document {} updated", tuple2.getT2().getInternalId()))
                        .map(_it -> new BsonDynamicModel(tuple2.getT2(), model.getModelName(), model.getModelData()))
                );
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
