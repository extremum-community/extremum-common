package common.dao;

import com.extremum.common.dao.impl.SpringDataMongoCommonDao;
import com.extremum.common.repository.SeesSoftlyDeletedRecords;
import models.TestModel;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestModelDao extends SpringDataMongoCommonDao<TestModel> {
    List<TestModel> findByName(String name);

    @SeesSoftlyDeletedRecords
    List<TestModel> findEvenDeletedByName(String name);
}
