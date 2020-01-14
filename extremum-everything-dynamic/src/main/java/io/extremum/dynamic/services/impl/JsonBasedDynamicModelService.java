package io.extremum.dynamic.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.extremum.common.exceptions.ModelNotFoundException;
import io.extremum.dynamic.dao.DynamicModelDao;
import io.extremum.dynamic.metadata.impl.DefaultJsonDynamicModelMetadataProvider;
import io.extremum.dynamic.models.impl.BsonDynamicModel;
import io.extremum.dynamic.models.impl.JsonDynamicModel;
import io.extremum.dynamic.services.DateTypesNormalizer;
import io.extremum.dynamic.services.DatesProcessor;
import io.extremum.dynamic.services.DynamicModelService;
import io.extremum.dynamic.validator.ValidationContext;
import io.extremum.dynamic.validator.services.impl.JsonDynamicModelValidator;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.io.IOException;
import java.util.Map;

import static reactor.core.publisher.Mono.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class JsonBasedDynamicModelService implements DynamicModelService<JsonDynamicModel> {
    private final DynamicModelDao<BsonDynamicModel> dao;
    private final JsonDynamicModelValidator modelValidator;
    private final DefaultJsonDynamicModelMetadataProvider metadataProvider;
    private final DateTypesNormalizer dateTypesNormalizer;
    private final DatesProcessor datesProcessor;
    private final ObjectMapper mapper;

    @Override
    public Mono<JsonDynamicModel> saveModel(JsonDynamicModel model) {
        return modelValidator.validate(model)
                .flatMap(either ->
                        either.fold(
                                Mono::error,
                                ctx -> saveValidatedModel(model, ctx)
                        )
                );
    }

    private Mono<JsonDynamicModel> saveValidatedModel(JsonDynamicModel model, ValidationContext ctx) {
        return fromSupplier(() -> {
            Document doc;
            try {
                doc = Document.parse(mapper.writeValueAsString(model.getModelData()));
            } catch (JsonProcessingException e) {
                String msg = String.format("Unable to read document as json %s", model.getModelData());
                log.error(msg, e);
                throw new RuntimeException(msg, e);
            }

            Map<String, Object> normalizedMap = dateTypesNormalizer.normalize(doc, ctx.getPaths());

            Document normalized = new Document(normalizedMap);

            return new BsonDynamicModel(model.getId(), model.getModelName(), normalized);
        })
                .flatMap(bModel -> getCollectionName(model).map(cName -> Tuples.of(bModel, cName)))
                .flatMap(tuple -> {
                            BsonDynamicModel bModel = tuple.getT1();
                            String collectionName = tuple.getT2();

                            if (bModel.getId() != null) {
                                return dao.replace(bModel, collectionName);
                            } else {
                                return dao.create(bModel, collectionName);
                            }
                        }
                ).map(this::toJsonDynamicModel);
    }

    private JsonDynamicModel toJsonDynamicModel(BsonDynamicModel bModel) {
        Document modelData = bModel.getModelData();

        Document documentWithReplacedDates = datesProcessor.processDates(modelData);

        return new JsonDynamicModel(bModel.getId(), bModel.getModelName(), toJson(documentWithReplacedDates));
    }

    private JsonNode toJson(Document modelData) {
        try {
            String json = mapper.writerFor(Map.class).writeValueAsString(modelData);

            return mapper.readValue(json, JsonNode.class);
        } catch (IOException e) {
            log.error("Unable to convert string to json from {}", modelData, e);
            throw new RuntimeException("Unable to parse json", e);
        }
    }

    @Override
    public Mono<JsonDynamicModel> findById(Descriptor id) {
        return getCollectionName(id)
                .flatMap(cName -> dao.getByIdFromCollection(id, cName))
                .map(this::toJsonDynamicModel)
                .map(metadataProvider::provideMetadata)
                .switchIfEmpty(error(new ModelNotFoundException("DynamicModel with id " + id + " not found")));
    }

    @Override
    public Mono<Void> remove(Descriptor id) {
        return getCollectionName(id)
                .flatMap(cName -> dao.remove(id, cName));
    }

    private Mono<String> getCollectionName(Descriptor descr) {
        return descr.getModelTypeReactively().map(this::normalizeStringToCollectionName);
    }

    private Mono<String> getCollectionName(JsonDynamicModel model) {
        if (model.getId() != null) {
            return getCollectionName(model.getId());
        } else {
            return just(model.getModelName())
                    .map(this::normalizeStringToCollectionName);
        }
    }

    private String normalizeStringToCollectionName(String str) {
        return str.toLowerCase().replaceAll("[\\W]", "_");
    }
}
