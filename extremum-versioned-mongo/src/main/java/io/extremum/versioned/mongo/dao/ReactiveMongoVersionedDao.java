package io.extremum.versioned.mongo.dao;

import io.extremum.common.dao.ReactiveCommonDao;
import io.extremum.versioned.mongo.model.MongoVersionedModel;
import org.bson.types.ObjectId;

public interface ReactiveMongoVersionedDao<M extends MongoVersionedModel> extends ReactiveCommonDao<M, ObjectId> {
}
