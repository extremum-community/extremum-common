package com.extremum.elastic.dao;

import com.extremum.elastic.factory.ElasticDescriptorFactory;
import com.extremum.elastic.properties.ElasticProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.extremum.elastic.model.TestElasticModel;

public class TestElasticModelDao extends DefaultElasticCommonDao<TestElasticModel> {
    public TestElasticModelDao(ElasticProperties elasticProperties,
            ElasticDescriptorFactory descriptorFactory,
            ObjectMapper mapper) {
        super(elasticProperties, descriptorFactory, mapper, TestElasticModel.INDEX, null);
    }
}
