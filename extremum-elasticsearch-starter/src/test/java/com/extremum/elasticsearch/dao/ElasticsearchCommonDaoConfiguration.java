package com.extremum.elasticsearch.dao;

import com.extremum.elasticsearch.factory.ElasticsearchDescriptorFactory;
import com.extremum.starter.CommonConfiguration;
import com.extremum.elasticsearch.properties.ElasticsearchProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author rpuch
 */
@Configuration
@EnableConfigurationProperties(ElasticsearchProperties.class)
@Import(CommonConfiguration.class)
@RequiredArgsConstructor
public class ElasticsearchCommonDaoConfiguration {
    private final ElasticsearchProperties elasticsearchProperties;

    @Bean
    public ElasticsearchDescriptorFactory elasticDescriptorFactory() {
        return new ElasticsearchDescriptorFactory();
    }

    @Bean
    public TestElasticsearchModelDao testElasticModelDao(ObjectMapper mapper) {
        return new TestElasticsearchModelDao(elasticsearchProperties, elasticDescriptorFactory(), mapper);
    }
}
