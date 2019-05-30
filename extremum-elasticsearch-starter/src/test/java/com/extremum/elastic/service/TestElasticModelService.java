package com.extremum.elastic.service;

import com.extremum.elastic.dao.ElasticCommonDao;
import com.extremum.elastic.service.impl.ElasticCommonServiceImpl;
import com.extremum.elastic.model.TestElasticModel;

/**
 * @author rpuch
 */
public class TestElasticModelService extends ElasticCommonServiceImpl<TestElasticModel> {
    public TestElasticModelService(ElasticCommonDao<TestElasticModel> dao) {
        super(dao);
    }
}
