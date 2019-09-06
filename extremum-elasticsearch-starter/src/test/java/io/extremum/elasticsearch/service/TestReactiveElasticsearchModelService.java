package io.extremum.elasticsearch.service;

import io.extremum.elasticsearch.dao.ReactiveElasticsearchCommonDao;
import io.extremum.elasticsearch.model.TestElasticsearchModel;
import io.extremum.elasticsearch.service.impl.ReactiveElasticsearchCommonServiceImpl;

/**
 * @author rpuch
 */
public class TestReactiveElasticsearchModelService
        extends ReactiveElasticsearchCommonServiceImpl<TestElasticsearchModel> {
    public TestReactiveElasticsearchModelService(ReactiveElasticsearchCommonDao<TestElasticsearchModel> dao) {
        super(dao);
    }
}
