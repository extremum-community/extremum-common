package io.extremum.dynamic.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.extremum.common.exceptions.ModelNotFoundException;
import io.extremum.common.utils.DateUtils;
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
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.lang.String.format;
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
        return fromSupplier(toBsonDynamicModel(model, ctx))
                .flatMap(findCollection(model))
                .flatMap(processWithDao())
                .map(toJsonDynamicModel());
    }

    @NotNull
    private Function<BsonDynamicModel, JsonDynamicModel> toJsonDynamicModel() {
        return bModel -> {
            Document modelData = bModel.getModelData();

            Document documentWithReplacedDates = datesProcessor.processDates(modelData);

            return new JsonDynamicModel(bModel.getId(), bModel.getModelName(), documentWithReplacedDates);
        };
    }

    private void updateModified(Document doc) {
        doc.replace("modified", getCurrentDateTimeAsString());
    }

    private void updateVersion(Document doc) {
        Object version = doc.get("version");
        if (version instanceof Integer) {
            doc.append("version", ((int) version) + 1);
        } else {
            log.error("Unknown version. Unable to determine version {}. Unable to update document {}",
                    version, doc);
            throw new RuntimeException("Unable to determine version of a document. Unable to update a document");
        }

        doc.remove("updated");
        doc.append("updated", getCurrentDateTimeAsString());
    }

    private void checkModelAttribute(JsonDynamicModel model, Document doc) {
        if (!doc.containsKey("model")) {
            throw new RuntimeException(format("Field %s must be defined", "model"));
        }

        if (!doc.getString("model").equals(model.getModelName())) {
            throw new RuntimeException(format("Field %s doesn't equal with %s", "model", model.getModelName()));
        }
    }

    private void initializeServiceFields(JsonDynamicModel model, Document doc) {
        String now = getCurrentDateTimeAsString();

        doc.append("created", now);
        doc.append("modified", now);
        doc.append("model", model.getModelName());
        doc.append("version", 1L);
    }

    private Function<Tuple2<BsonDynamicModel, String>, Mono<? extends BsonDynamicModel>> processWithDao() {
        return tuple -> {
            BsonDynamicModel bModel = tuple.getT1();
            String collectionName = tuple.getT2();

            if (isNewModel(bModel)) {
                return dao.create(bModel, collectionName);
            } else {
                return dao.replace(bModel, collectionName);
            }
        };
    }

    private Function<BsonDynamicModel, Mono<? extends Tuple2<BsonDynamicModel, String>>> findCollection(JsonDynamicModel model) {
        return bModel -> getCollectionName(model).map(cName -> Tuples.of(bModel, cName));
    }

    private Supplier<BsonDynamicModel> toBsonDynamicModel(JsonDynamicModel model, ValidationContext ctx) {
        return () -> {
            Document doc;
            try {
                doc = Document.parse(mapper.writeValueAsString(model.getModelData()));
            } catch (JsonProcessingException e) {
                String msg = String.format("Unable to read document as json %s", model.getModelData());
                log.error(msg, e);
                throw new RuntimeException(msg, e);
            }

            if (isNewModel(model)) {
                initializeServiceFields(model, doc);
            } else {
                checkModelAttribute(model, doc);
                updateModified(doc);
                updateVersion(doc);
            }

            Map<String, Object> normalizedMap = dateTypesNormalizer.normalize(doc, ctx.getPaths());

            Document normalized = new Document(normalizedMap);

            return new BsonDynamicModel(model.getId(), model.getModelName(), normalized);
        };
    }

    private String getCurrentDateTimeAsString() {
        return DateUtils.formatZonedDateTimeISO_8601(ZonedDateTime.now());
    }

    private boolean isNewModel(JsonDynamicModel model) {
        return model.getId() == null;
    }

    private boolean isNewModel(BsonDynamicModel model) {
        return model.getId() == null;
    }

    @Override
    public Mono<JsonDynamicModel> findById(Descriptor id) {
        return getCollectionName(id)
                .flatMap(cName -> dao.getByIdFromCollection(id, cName))
                .map(toJsonDynamicModel())
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
