package io.extremum.jpa.services;

import io.extremum.jpa.dao.PostgresCommonDao;
import io.extremum.jpa.models.TestJpaModel;
import io.extremum.jpa.services.impl.PostgresCommonServiceImpl;

/**
 * @author rpuch
 */
public class TestJpaModelService extends PostgresCommonServiceImpl<TestJpaModel> {
    public TestJpaModelService(PostgresCommonDao<TestJpaModel> dao) {
        super(dao);
    }
}
