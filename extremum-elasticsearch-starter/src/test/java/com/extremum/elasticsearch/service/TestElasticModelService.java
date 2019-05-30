package com.extremum.elasticsearch.service;

import com.extremum.elasticsearch.dao.ElasticCommonDao;
import com.extremum.elasticsearch.service.impl.ElasticCommonServiceImpl;
import com.extremum.elasticsearch.model.TestElasticModel;

/**
 * @author rpuch
 */
public class TestElasticModelService extends ElasticCommonServiceImpl<TestElasticModel> {
    public TestElasticModelService(ElasticCommonDao<TestElasticModel> dao) {
        super(dao);
    }
}
