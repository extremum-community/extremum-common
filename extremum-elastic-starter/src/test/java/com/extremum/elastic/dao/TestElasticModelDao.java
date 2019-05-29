package com.extremum.elastic.dao;

import com.extremum.common.dao.DefaultElasticCommonDao;
import com.extremum.common.descriptor.factory.impl.ElasticDescriptorFactory;
import com.extremum.starter.properties.ElasticProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.extremum.elastic.model.TestElasticModel;

public class TestElasticModelDao extends DefaultElasticCommonDao<TestElasticModel> {
    public TestElasticModelDao(ElasticProperties elasticProperties,
            ElasticDescriptorFactory descriptorFactory,
            ObjectMapper mapper) {
        super(elasticProperties, descriptorFactory, mapper, "test_entities", null);
    }
}
