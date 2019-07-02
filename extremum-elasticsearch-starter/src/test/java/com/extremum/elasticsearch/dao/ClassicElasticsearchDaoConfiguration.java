package com.extremum.elasticsearch.dao;

import com.extremum.common.descriptor.service.DescriptorService;
import com.extremum.elasticsearch.config.ElasticsearchRepositoriesConfiguration;
import com.extremum.elasticsearch.factory.ElasticsearchDescriptorFactory;
import com.extremum.elasticsearch.properties.ElasticsearchProperties;
import com.extremum.starter.CommonConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author rpuch
 */
@Configuration
@Import({CommonConfiguration.class, ElasticsearchRepositoriesConfiguration.class})
public class ClassicElasticsearchDaoConfiguration {
    @Bean
    public ClassicTestElasticsearchModelDao classicTestElasticsearchModelDao(
            ElasticsearchProperties elasticsearchProperties,
            DescriptorService descriptorService,
            ElasticsearchDescriptorFactory elasticsearchDescriptorFactory,
            ObjectMapper objectMapper) {
        return new ClassicTestElasticsearchModelDao(elasticsearchProperties, descriptorService,
                elasticsearchDescriptorFactory, objectMapper);
    }
}
