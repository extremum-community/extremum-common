package com.extremum.elasticsearch.dao;

import com.extremum.elasticsearch.factory.ElasticDescriptorFactory;
import com.extremum.elasticsearch.properties.ElasticProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.extremum.elasticsearch.model.TestElasticModel;

public class TestElasticModelDao extends DefaultElasticCommonDao<TestElasticModel> {
    public TestElasticModelDao(ElasticProperties elasticProperties,
            ElasticDescriptorFactory descriptorFactory,
            ObjectMapper mapper) {
        super(elasticProperties, descriptorFactory, mapper, TestElasticModel.INDEX, null);
    }
}
