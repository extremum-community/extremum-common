package com.extremum.jpa.services;

import com.extremum.jpa.dao.PostgresCommonDao;
import com.extremum.jpa.models.TestBasicJpaModel;
import com.extremum.jpa.services.impl.PostgresBasicServiceImpl;

/**
 * @author rpuch
 */
public class TestBasicJpaModelServiceImpl extends PostgresBasicServiceImpl<TestBasicJpaModel>
        implements TestBasicJpaModelService {
    public TestBasicJpaModelServiceImpl(PostgresCommonDao<TestBasicJpaModel> dao) {
        super(dao);
    }
}
