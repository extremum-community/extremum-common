package common.dao;

import com.extremum.common.dao.MongoCommonDao;
import models.TestModel;
import org.mongodb.morphia.Datastore;


public class TestModelDao extends MongoCommonDao<TestModel> {
    public TestModelDao(Datastore datastore) {
        super(datastore);
    }
}
