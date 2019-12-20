package io.extremum.dynamic.services.impl;

import io.extremum.dynamic.dao.MongoJsonDynamicModelDao;
import io.extremum.dynamic.models.impl.JsonDynamicModel;
import io.extremum.dynamic.services.DynamicModelService;
import io.extremum.dynamic.validator.exceptions.DynamicModelValidationException;
import io.extremum.dynamic.validator.exceptions.SchemaNotFoundException;
import io.extremum.dynamic.validator.services.impl.JsonDynamicModelValidator;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class JsonBasedDynamicModelService implements DynamicModelService<JsonDynamicModel> {
    private final MongoJsonDynamicModelDao dao;
    private final JsonDynamicModelValidator modelValidator;

    @Override
    public Mono<JsonDynamicModel> saveModel(JsonDynamicModel model) {
        try {
            modelValidator.validate(model);

            String collectionName = getCollectionName(model);

            return dao.save(model, collectionName);
        } catch (DynamicModelValidationException e) {
            log.error("Model {} is not valid", model, e);
            return Mono.error(e);
        } catch (SchemaNotFoundException e) {
            log.error("Schema not found for model {}", model, e);
            return Mono.error(e);
        }
    }

    @Override
    public Mono<JsonDynamicModel> findById(Descriptor id) {
        return dao.getByIdFromCollection(id, getCollectionName(id));
    }

    private String getCollectionName(Descriptor descr) {
        return normalizeStringToCollectionName(descr.getModelType());
    }

    private String getCollectionName(JsonDynamicModel model) {
        if (model.getId() != null) {
            return getCollectionName(model.getId());
        } else {
            return normalizeStringToCollectionName(model.getModelName());
        }
    }

    private String normalizeStringToCollectionName(String str) {
        return str.toLowerCase().replaceAll("[\\W]", "_");
    }
}
