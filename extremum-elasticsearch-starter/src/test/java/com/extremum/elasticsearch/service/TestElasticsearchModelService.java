package com.extremum.elasticsearch.service;

import com.extremum.elasticsearch.dao.ElasticsearchCommonDao;
import com.extremum.elasticsearch.service.impl.ElasticsearchCommonServiceImpl;
import com.extremum.elasticsearch.model.TestElasticsearchModel;

/**
 * @author rpuch
 */
public class TestElasticsearchModelService extends ElasticsearchCommonServiceImpl<TestElasticsearchModel> {
    public TestElasticsearchModelService(ElasticsearchCommonDao<TestElasticsearchModel> dao) {
        super(dao);
    }
}
