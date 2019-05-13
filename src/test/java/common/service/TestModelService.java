package common.service;

import com.extremum.common.dao.MongoCommonDao;
import com.extremum.common.service.impl.MongoCommonServiceImpl;
import models.TestModel;

/**
 * @author rpuch
 */
public class TestModelService extends MongoCommonServiceImpl<TestModel> {
    public TestModelService(MongoCommonDao<TestModel> dao) {
        super(dao);
    }
}
