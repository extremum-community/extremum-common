package common.service.jpa;

import com.extremum.common.dao.PostgresCommonDao;
import com.extremum.common.service.impl.PostgresBasicServiceImpl;
import models.TestBasicJpaModel;

/**
 * @author rpuch
 */
public class TestBasicJpaModelServiceImpl extends PostgresBasicServiceImpl<TestBasicJpaModel>
        implements TestBasicJpaModelService {
    public TestBasicJpaModelServiceImpl(PostgresCommonDao<TestBasicJpaModel> dao) {
        super(dao);
    }
}
