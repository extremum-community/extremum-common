package common.dao.jpa;

import com.extremum.common.dao.impl.SpringDataJpaCommonDao;
import models.TestBasicJpaModel;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestBasicJpaModelDao extends SpringDataJpaCommonDao<TestBasicJpaModel> {
    List<TestBasicJpaModel> findByName(String name);
}
