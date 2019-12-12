package io.extremum.dynamic.dao.impl;

import io.extremum.dynamic.dao.DynamicModelDao;
import io.extremum.dynamic.models.impl.JsonDynamicModel;
import reactor.core.publisher.Mono;

public class MongoDynamicModelDao implements DynamicModelDao<JsonDynamicModel> {
    @Override
    public Mono<JsonDynamicModel> save(JsonDynamicModel model) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
