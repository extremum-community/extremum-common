package io.extremum.dynamic.services.impl;

import io.extremum.dynamic.MongoSchemaPointer;
import io.extremum.dynamic.dao.impl.MongoDynamicModelDao;
import io.extremum.dynamic.models.impl.JsonBasedDynamicModel;
import io.extremum.dynamic.schema.networknt.MongoBasedSchemaProvider;
import io.extremum.dynamic.schema.networknt.NetworkntSchema;
import io.extremum.dynamic.services.DynamicModelService;
import io.extremum.dynamic.validator.services.impl.JsonBasedDynamicModelValidator;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class JsonBasedMongoDynamicModelService implements DynamicModelService<JsonBasedDynamicModel, MongoSchemaPointer> {
    private final MongoDynamicModelDao dao;
    private final MongoBasedSchemaProvider schemaProvider;
    private final JsonBasedDynamicModelValidator modelValidator;

    @Override
    public Mono<JsonBasedDynamicModel> saveModel(MongoSchemaPointer pointer, JsonBasedDynamicModel model) {
        NetworkntSchema schema = schemaProvider.loadSchema(pointer);

        modelValidator.validate(model, schema);

        return dao.save(model);
    }
}
