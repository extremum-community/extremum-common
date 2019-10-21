package io.extremum.mongo.service.impl;

import io.extremum.common.dao.ReactiveCommonDao;
import io.extremum.common.service.impl.ReactiveCommonServiceImpl;
import io.extremum.mongo.model.MongoVersionedModel;
import io.extremum.mongo.service.ReactiveMongoVersionedService;
import org.bson.types.ObjectId;

public abstract class ReactiveMongoVersionedServiceImpl<M extends MongoVersionedModel>
        extends ReactiveCommonServiceImpl<ObjectId, M> implements ReactiveMongoVersionedService<M> {
    public ReactiveMongoVersionedServiceImpl(ReactiveCommonDao<M, ObjectId> dao) {
        super(dao);
    }

    @Override
    protected ObjectId stringToId(String id) {
        return new ObjectId(id);
    }
}
