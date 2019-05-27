package common.service.jpa;

import com.extremum.common.dao.PostgresCommonDao;
import com.extremum.common.service.impl.PostgresCommonServiceImpl;
import models.TestJpaModel;

/**
 * @author rpuch
 */
public class TestJpaModelService extends PostgresCommonServiceImpl<TestJpaModel> {
    public TestJpaModelService(PostgresCommonDao<TestJpaModel> dao) {
        super(dao);
    }
}
