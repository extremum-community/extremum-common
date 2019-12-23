package io.extremum.dynamic.services.impl;

import io.extremum.common.exceptions.ModelNotFoundException;
import io.extremum.dynamic.dao.MongoJsonDynamicModelDao;
import io.extremum.dynamic.metadata.impl.DefaultJsonDynamicModelMetadataProvider;
import io.extremum.dynamic.models.impl.JsonDynamicModel;
import io.extremum.dynamic.services.DynamicModelService;
import io.extremum.dynamic.validator.services.impl.JsonDynamicModelValidator;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static reactor.core.publisher.Mono.error;

@Slf4j
@Service
@RequiredArgsConstructor
public class JsonBasedDynamicModelService implements DynamicModelService<JsonDynamicModel> {
    private final MongoJsonDynamicModelDao dao;
    private final JsonDynamicModelValidator modelValidator;
    private final DefaultJsonDynamicModelMetadataProvider metadataProvider;

    @Override
    public Mono<JsonDynamicModel> saveModel(JsonDynamicModel model) {
        return modelValidator.validate(model)
                .flatMap(either ->
                        either.fold(
                                Mono::error,
                                _it -> saveValidatedModel(model)
                        )
                );
    }

    private Mono<JsonDynamicModel> saveValidatedModel(JsonDynamicModel model) {
        return getCollectionName(model).flatMap(cName -> dao.save(model, cName));
    }

    @Override
    public Mono<JsonDynamicModel> findById(Descriptor id) {
        return getCollectionName(id)
                .flatMap(cName -> dao.getByIdFromCollection(id, cName))
                .map(metadataProvider::provideMetadata)
                .switchIfEmpty(error(new ModelNotFoundException("DynamicModel with id " + id + " not found")));
    }

    private Mono<String> getCollectionName(Descriptor descr) {
        return descr.getModelTypeReactively().map(this::normalizeStringToCollectionName);
    }

    private Mono<String> getCollectionName(JsonDynamicModel model) {
        if (model.getId() != null) {
            return getCollectionName(model.getId());
        } else {
            return Mono.just(model.getModelName())
                    .map(this::normalizeStringToCollectionName);
        }
    }

    private String normalizeStringToCollectionName(String str) {
        return str.toLowerCase().replaceAll("[\\W]", "_");
    }
}
