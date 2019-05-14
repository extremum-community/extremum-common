package common.dao.jpa;

import com.extremum.common.dao.impl.SpringDataJpaCommonDao;
import com.extremum.common.repository.SeesSoftlyDeletedRecords;
import models.TestJpaModel;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestJpaModelDao extends SpringDataJpaCommonDao<TestJpaModel> {
    List<TestJpaModel> findByName(String name);

    @SeesSoftlyDeletedRecords
    List<TestJpaModel> findEvenDeletedByName(String name);

    long countByName(String name);

    @SeesSoftlyDeletedRecords
    long countEvenDeletedByName(String name);
}
