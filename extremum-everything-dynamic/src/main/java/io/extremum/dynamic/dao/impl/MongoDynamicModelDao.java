package io.extremum.dynamic.dao.impl;

import io.extremum.dynamic.dao.DynamicModelDao;
import io.extremum.dynamic.models.impl.JsonBasedDynamicModel;
import reactor.core.publisher.Mono;

public class MongoDynamicModelDao implements DynamicModelDao<JsonBasedDynamicModel> {
    @Override
    public Mono<JsonBasedDynamicModel> save(JsonBasedDynamicModel model) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
