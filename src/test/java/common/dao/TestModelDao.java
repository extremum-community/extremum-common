package common.dao;

import com.extremum.common.dao.impl.SpringDataMongoCommonDao;
import models.TestModel;
import org.springframework.stereotype.Repository;

@Repository
public interface TestModelDao extends SpringDataMongoCommonDao<TestModel> {
}
