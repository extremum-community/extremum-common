package io.extremum.dynamic.dao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.reactivestreams.client.FindPublisher;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.Success;
import io.extremum.common.exceptions.ModelNotFoundException;
import io.extremum.dynamic.models.impl.JsonDynamicModel;
import io.extremum.mongo.facilities.MongoDescriptorFacilities;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Function;

@Slf4j
@Component
@RequiredArgsConstructor
public class MongoJsonDynamicModelDao {
    private final ReactiveMongoOperations mongoOperations;
    private final MongoDescriptorFacilities mongoDescriptorFacilities;

    public Mono<JsonDynamicModel> save(JsonDynamicModel model, String collectionName) {
        final Descriptor modelDescriptor = Optional.ofNullable(model.getId())
                .orElseGet(() -> createNewDescriptor(model));

        Document newDocument = Document.parse(model.getModelData().toString())
                .append("_id", new ObjectId(modelDescriptor.getInternalId()));

        return Mono.just(mongoOperations.getCollection(collectionName))
                .flatMap(createDocumentInCollection(newDocument))
                .doOnNext(successPublisher ->
                        log.info("Document {} saved", modelDescriptor.getInternalId()))
                .map(_it -> new JsonDynamicModel(modelDescriptor, model.getModelName(), model.getModelData()));
    }

    public Mono<JsonDynamicModel> getByIdFromCollection(Descriptor id, String collectionName) {
        FindPublisher<Document> p = mongoOperations.getCollection(collectionName)
                .find(new Document("_id", new ObjectId(id.getInternalId())));

        return Mono.from(p)
                .map(doc -> {
                    Descriptor descr = mongoDescriptorFacilities.fromInternalId(doc.getObjectId("_id").toString());
                    doc.remove("_id");
                    return new JsonDynamicModel(descr, descr.getModelType(), toNode(doc.toJson()));
                }).switchIfEmpty(Mono.error(new ModelNotFoundException("DynamicModel with id " + id + " not found")));
    }

    private Function<MongoCollection<Document>, Mono<Success>> createDocumentInCollection(Document document) {
        return collection -> Mono.from(collection.insertOne(document));
    }

    private Descriptor createNewDescriptor(JsonDynamicModel model) {
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
