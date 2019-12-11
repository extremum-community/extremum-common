package io.extremum.dynamic.services.impl;

import io.extremum.dynamic.dao.impl.MongoDynamicModelDao;
import io.extremum.dynamic.models.impl.JsonBasedDynamicModel;
import io.extremum.dynamic.services.DynamicModelService;
import io.extremum.dynamic.validator.exceptions.SchemaValidationException;
import io.extremum.dynamic.validator.services.DynamicModelValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public class JsonBasedDynamicModelService implements DynamicModelService<JsonBasedDynamicModel> {
    private final MongoDynamicModelDao dao;
    private final DynamicModelValidator<JsonBasedDynamicModel> modelValidator;

    @Override
    public Mono<JsonBasedDynamicModel> saveModel(JsonBasedDynamicModel model) {
        try {
            modelValidator.validate(model);
            return dao.save(model);
        } catch (SchemaValidationException e) {
            log.error("Model {} is not valid", model, e);
            return Mono.error(e);
        }
    }
}
