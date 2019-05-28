package com.extremum.services;

import com.extremum.dao.PostgresCommonDao;
import com.extremum.models.TestBasicJpaModel;
import com.extremum.services.impl.PostgresBasicServiceImpl;

/**
 * @author rpuch
 */
public class TestBasicJpaModelServiceImpl extends PostgresBasicServiceImpl<TestBasicJpaModel>
        implements TestBasicJpaModelService {
    public TestBasicJpaModelServiceImpl(PostgresCommonDao<TestBasicJpaModel> dao) {
        super(dao);
    }
}
