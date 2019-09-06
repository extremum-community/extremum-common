package common.service.mongo;

import io.extremum.mongo.dao.MongoCommonDao;
import io.extremum.mongo.dao.ReactiveMongoCommonDao;
import io.extremum.mongo.service.impl.MongoCommonServiceImpl;
import io.extremum.mongo.service.impl.ReactiveMongoCommonServiceImpl;
import models.TestMongoModel;

/**
 * @author rpuch
 */
public class TestReactiveMongoModelService extends ReactiveMongoCommonServiceImpl<TestMongoModel> {
    public TestReactiveMongoModelService(ReactiveMongoCommonDao<TestMongoModel> dao) {
        super(dao);
    }
}
