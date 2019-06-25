package com.extremum.elasticsearch.dao;

import com.extremum.elasticsearch.factory.ElasticsearchDescriptorFactory;
import com.extremum.elasticsearch.model.TestElasticsearchModel;
import com.extremum.elasticsearch.properties.ElasticsearchProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClassicTestElasticsearchModelDao extends DefaultElasticsearchCommonDao<TestElasticsearchModel> {
    public ClassicTestElasticsearchModelDao(
            ElasticsearchProperties elasticsearchProperties,
            ElasticsearchDescriptorFactory descriptorFactory,
            ObjectMapper mapper) {
        super(elasticsearchProperties, descriptorFactory, mapper, TestElasticsearchModel.INDEX, "_doc");
    }
}