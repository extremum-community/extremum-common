package com.extremum.elasticsearch.dao;

import com.extremum.elasticsearch.factory.ElasticsearchDescriptorFactory;
import com.extremum.elasticsearch.properties.ElasticsearchProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.extremum.elasticsearch.model.TestElasticsearchModel;

public class TestElasticsearchModelDao extends DefaultElasticsearchCommonDao<TestElasticsearchModel> {
    public TestElasticsearchModelDao(ElasticsearchProperties elasticsearchProperties,
            ElasticsearchDescriptorFactory descriptorFactory,
            ObjectMapper mapper) {
        super(elasticsearchProperties, descriptorFactory, mapper, TestElasticsearchModel.INDEX, null);
    }
}
