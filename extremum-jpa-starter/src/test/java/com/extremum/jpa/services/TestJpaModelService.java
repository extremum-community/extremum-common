package com.extremum.jpa.services;

import com.extremum.jpa.dao.PostgresCommonDao;
import com.extremum.jpa.models.TestJpaModel;
import com.extremum.jpa.services.impl.PostgresCommonServiceImpl;

/**
 * @author rpuch
 */
public class TestJpaModelService extends PostgresCommonServiceImpl<TestJpaModel> {
    public TestJpaModelService(PostgresCommonDao<TestJpaModel> dao) {
        super(dao);
    }
}
