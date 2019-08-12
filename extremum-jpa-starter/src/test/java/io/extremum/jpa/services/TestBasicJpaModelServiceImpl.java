package io.extremum.jpa.services;

import io.extremum.jpa.dao.PostgresCommonDao;
import io.extremum.jpa.models.TestBasicJpaModel;
import io.extremum.jpa.services.impl.PostgresBasicServiceImpl;

/**
 * @author rpuch
 */
public class TestBasicJpaModelServiceImpl extends PostgresBasicServiceImpl<TestBasicJpaModel>
        implements TestBasicJpaModelService {
    public TestBasicJpaModelServiceImpl(PostgresCommonDao<TestBasicJpaModel> dao) {
        super(dao);
    }
}
