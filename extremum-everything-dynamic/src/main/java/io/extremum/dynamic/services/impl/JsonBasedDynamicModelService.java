package io.extremum.dynamic.services.impl;

import io.extremum.common.exceptions.ModelNotFoundException;
import io.extremum.dynamic.dao.DynamicModelDao;
import io.extremum.dynamic.metadata.impl.DefaultJsonDynamicModelMetadataProvider;
import io.extremum.dynamic.models.impl.JsonDynamicModel;
import io.extremum.dynamic.services.DateTypesNormalizer;
import io.extremum.dynamic.services.DatesProcessor;
import io.extremum.dynamic.services.DynamicModelService;
import io.extremum.dynamic.validator.ValidationContext;
import io.extremum.dynamic.validator.services.impl.JsonDynamicModelValidator;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static reactor.core.publisher.Mono.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class JsonBasedDynamicModelService implements DynamicModelService<JsonDynamicModel> {
    private final DynamicModelDao<JsonDynamicModel> dao;
    private final JsonDynamicModelValidator modelValidator;
    private final DefaultJsonDynamicModelMetadataProvider metadataProvider;
    private final DateTypesNormalizer dateTypesNormalizer;
    private final DatesProcessor datesProcessor;

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
        return fromSupplier(normalize(model, ctx))
                .flatMap(findCollection(model))
                .flatMap(processWithDao());
    }

    @NotNull
    private Function<JsonDynamicModel, JsonDynamicModel> normalize() {
        return bModel -> {
            Map<String, Object> mapWithReplacedDates = datesProcessor.processDates(bModel.getModelData());

            return new JsonDynamicModel(bModel.getId(), bModel.getModelName(), mapWithReplacedDates);
        };
    }

    private Function<Tuple2<JsonDynamicModel, String>, Mono<? extends JsonDynamicModel>> processWithDao() {
        return tuple -> {
            JsonDynamicModel bModel = tuple.getT1();
            String collectionName = tuple.getT2();

            if (isNewModel(bModel)) {
                return dao.create(bModel, collectionName);
            } else {
                return dao.replace(bModel, collectionName);
            }
        };
    }

    private Function<JsonDynamicModel, Mono<? extends Tuple2<JsonDynamicModel, String>>> findCollection(JsonDynamicModel model) {
        return bModel -> getCollectionName(model).map(cName -> Tuples.of(bModel, cName));
    }

    private Supplier<JsonDynamicModel> normalize(JsonDynamicModel model, ValidationContext ctx) {
        return () -> {
            dateTypesNormalizer.normalize(model.getModelData(), ctx.getPaths());
            return model;
        };
    }

    private boolean isNewModel(JsonDynamicModel model) {
        return model.getId() == null;
    }

    @Override
    public Mono<JsonDynamicModel> findById(Descriptor id) {
        return getCollectionName(id)
                .flatMap(cName -> dao.getByIdFromCollection(id, cName))
                .map(normalize())
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
