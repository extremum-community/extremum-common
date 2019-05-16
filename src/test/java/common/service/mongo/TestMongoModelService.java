package common.service.mongo;

import com.extremum.common.dao.MongoCommonDao;
import com.extremum.common.service.impl.MongoCommonServiceImpl;
import models.TestMongoModel;

/**
 * @author rpuch
 */
public class TestMongoModelService extends MongoCommonServiceImpl<TestMongoModel> {
    public TestMongoModelService(MongoCommonDao<TestMongoModel> dao) {
        super(dao);
    }
}
