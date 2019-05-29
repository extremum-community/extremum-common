package common.dao.mongo;

import com.extremum.common.dao.impl.SpringDataMongoCommonDao;
import com.extremum.common.repository.SeesSoftlyDeletedRecords;
import models.TestMongoModel;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestMongoModelDao extends SpringDataMongoCommonDao<TestMongoModel> {
    List<TestMongoModel> findByName(String name);

    @SeesSoftlyDeletedRecords
    List<TestMongoModel> findEvenDeletedByName(String name);

    long countByName(String name);

    @SeesSoftlyDeletedRecords
    long countEvenDeletedByName(String name);
}
