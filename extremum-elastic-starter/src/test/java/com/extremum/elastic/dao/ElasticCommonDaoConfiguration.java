package com.extremum.elastic.dao;

import com.extremum.elastic.factory.ElasticDescriptorFactory;
import com.extremum.starter.CommonConfiguration;
import com.extremum.elastic.properties.ElasticProperties;
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
@EnableConfigurationProperties(ElasticProperties.class)
@Import(CommonConfiguration.class)
@RequiredArgsConstructor
public class ElasticCommonDaoConfiguration {
    private final ElasticProperties elasticProperties;

    @Bean
    public ElasticDescriptorFactory elasticDescriptorFactory() {
        return new ElasticDescriptorFactory();
    }

    @Bean
    public TestElasticModelDao testElasticModelDao(ObjectMapper mapper) {
        return new TestElasticModelDao(elasticProperties, elasticDescriptorFactory(), mapper);
    }
}
