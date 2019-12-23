package io.extremum.dynamic.dao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.reactivestreams.client.FindPublisher;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.Success;
import io.extremum.dynamic.models.impl.JsonDynamicModel;
import io.extremum.mongo.facilities.ReactiveMongoDescriptorFacilities;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Function;

import static reactor.core.publisher.Mono.*;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MongoJsonDynamicModelDao {
    private final ReactiveMongoOperations mongoOperations;
    private final ReactiveMongoDescriptorFacilities mongoDescriptorFacilities;

    public Mono<JsonDynamicModel> create(JsonDynamicModel model, String collectionName) {
        return justOrEmpty(model.getId())
                .switchIfEmpty(createNewDescriptor(model))
                .map(descriptor -> {
                            Document doc = Document.parse(model.getModelData().toString())
                                    .append("_id", new ObjectId(descriptor.getInternalId()));
                            return Tuples.of(doc, descriptor);
                        }
                ).flatMap(tuple2 -> just(mongoOperations.getCollection(collectionName))
                        .flatMap(createDocumentInCollection(tuple2.getT1()))
                        .doOnNext(successPublisher ->
                                log.info("Document {} saved", tuple2.getT2().getInternalId()))
                        .map(_it -> new JsonDynamicModel(tuple2.getT2(), model.getModelName(), model.getModelData()))
                );
    }

    public Mono<JsonDynamicModel> replace(JsonDynamicModel model, String collectionName) {
        Objects.requireNonNull(model.getId(), "ID of a model can't be null");

        return just(model.getId())
                .map(descriptor -> {
                            Document doc = Document.parse(model.getModelData().toString())
                                    .append("_id", new ObjectId(descriptor.getInternalId()));
                            return Tuples.of(doc, descriptor);
                        }
                ).flatMap(tuple2 -> just(mongoOperations.getCollection(collectionName))
                        .flatMap(replaceDocumentInCollection(tuple2.getT1()))
                        .doOnNext(updatePublisher ->
                                log.info("Document {} updated", tuple2.getT2().getInternalId()))
                        .map(_it -> new JsonDynamicModel(tuple2.getT2(), model.getModelName(), model.getModelData()))
                );
    }

    public Mono<JsonDynamicModel> getByIdFromCollection(Descriptor id, String collectionName) {
        FindPublisher<Document> p = mongoOperations.getCollection(collectionName)
                .find(new Document("_id", new ObjectId(id.getInternalId())));

        return from(p)
                .flatMap(doc ->
                        mongoDescriptorFacilities
                                .fromInternalId(doc.getObjectId("_id"))
                                .map(descr -> {
                                    doc.remove("_id");
                                    return new JsonDynamicModel(descr, descr.getModelType(), toNode(doc.toJson()));
                                })
                );
    }

    private Function<MongoCollection<Document>, Mono<UpdateResult>> replaceDocumentInCollection(Document doc) {
        return collection -> from(collection.replaceOne(
                new Document("_id", doc.getObjectId("_id")),
                doc
        ));
    }

    private Function<MongoCollection<Document>, Mono<Success>> createDocumentInCollection(Document document) {
        return collection -> from(collection.insertOne(document));
    }

    private Mono<Descriptor> createNewDescriptor(JsonDynamicModel model) {
        return mongoDescriptorFacilities.create(ObjectId.get(), model.getModelName());
    }

    private JsonNode toNode(String toJson) {
        try {
            return new ObjectMapper().readValue(toJson, JsonNode.class);
        } catch (IOException e) {
            log.error("Unable to deserialize a document {}", toJson);
            throw new RuntimeException("Unable extract a dynamic model", e);
        }
    }
}
