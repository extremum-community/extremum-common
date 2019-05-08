package common.dao;

import com.extremum.common.dao.MongoCommonDao;
import models.TestModel;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestModelDao extends MongoCommonDao<TestModel>, MongoRepository<TestModel, ObjectId> {
}
