package io.extremum.dynamic.services.impl;

import io.extremum.dynamic.dao.impl.MongoDynamicModelDao;
import io.extremum.dynamic.models.impl.JsonDynamicModel;
import io.extremum.dynamic.services.DynamicModelService;
import io.extremum.dynamic.validator.exceptions.DynamicModelValidationException;
import io.extremum.dynamic.validator.exceptions.SchemaNotFoundException;
import io.extremum.dynamic.validator.services.impl.JsonDynamicModelValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public class JsonBasedDynamicModelService implements DynamicModelService<JsonDynamicModel> {
    private final MongoDynamicModelDao dao;
    private final JsonDynamicModelValidator modelValidator;

    @Override
    public Mono<JsonDynamicModel> saveModel(JsonDynamicModel model) {
        try {
            modelValidator.validate(model);
            return dao.save(model);
        } catch (DynamicModelValidationException e) {
            log.error("Model {} is not valid", model, e);
            return Mono.error(e);
        } catch (SchemaNotFoundException e) {
            log.error("Schema not found for model {}", model, e);
            return Mono.error(e);
        }
    }
}
