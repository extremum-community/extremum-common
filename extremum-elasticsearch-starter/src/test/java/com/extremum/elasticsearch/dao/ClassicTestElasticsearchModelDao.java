package com.extremum.elasticsearch.dao;

import com.extremum.common.descriptor.service.DescriptorService;
import com.extremum.elasticsearch.factory.ElasticsearchDescriptorFacilities;
import com.extremum.elasticsearch.model.TestElasticsearchModel;
import com.extremum.elasticsearch.properties.ElasticsearchProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClassicTestElasticsearchModelDao extends DefaultElasticsearchCommonDao<TestElasticsearchModel> {
    public ClassicTestElasticsearchModelDao(
            ElasticsearchProperties elasticsearchProperties,
            DescriptorService descriptorService,
            ElasticsearchDescriptorFacilities descriptorFacilities,
            ObjectMapper mapper) {
        super(elasticsearchProperties, descriptorService, descriptorFacilities, mapper,
                TestElasticsearchModel.INDEX, "_doc");
    }
}
