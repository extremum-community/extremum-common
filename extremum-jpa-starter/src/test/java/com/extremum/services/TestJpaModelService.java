package com.extremum.services;

import com.extremum.dao.PostgresCommonDao;
import com.extremum.models.TestJpaModel;
import com.extremum.services.impl.PostgresCommonServiceImpl;

/**
 * @author rpuch
 */
public class TestJpaModelService extends PostgresCommonServiceImpl<TestJpaModel> {
    public TestJpaModelService(PostgresCommonDao<TestJpaModel> dao) {
        super(dao);
    }
}
