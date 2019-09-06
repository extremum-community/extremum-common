package io.extremum.mongo.service.impl;

import io.extremum.common.service.impl.ReactiveCommonServiceImpl;
import io.extremum.mongo.dao.ReactiveMongoCommonDao;
import io.extremum.mongo.model.MongoCommonModel;
import io.extremum.mongo.service.ReactiveMongoCommonService;
import org.bson.types.ObjectId;


public class ReactiveMongoCommonServiceImpl<M extends MongoCommonModel> extends ReactiveCommonServiceImpl<ObjectId, M>
        implements ReactiveMongoCommonService<M> {

    public ReactiveMongoCommonServiceImpl(ReactiveMongoCommonDao<M> dao) {
        super(dao);
    }

    @Override
    protected ObjectId stringToId(String id) {
        return new ObjectId(id);
    }
}
