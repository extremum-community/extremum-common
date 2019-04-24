package common.dao;

import com.extremum.common.dao.MorphiaMongoCommonDao;
import models.TestModel;
import org.mongodb.morphia.Datastore;


public class TestModelDao extends MorphiaMongoCommonDao<TestModel> {
    public TestModelDao(Datastore datastore) {
        super(datastore);
    }
}
