package io.extremum.versioned.mongo.service;

import io.extremum.common.service.ReactiveCommonService;
import io.extremum.versioned.mongo.model.MongoVersionedModel;

public interface ReactiveMongoVersionedService<M extends MongoVersionedModel> extends ReactiveCommonService<M> {
}
