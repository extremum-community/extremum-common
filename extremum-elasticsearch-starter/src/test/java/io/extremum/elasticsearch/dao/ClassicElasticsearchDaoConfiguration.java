package io.extremum.elasticsearch.dao;

import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.elasticsearch.config.ElasticsearchRepositoriesConfiguration;
import io.extremum.elasticsearch.facilities.ElasticsearchDescriptorFacilities;
import io.extremum.elasticsearch.properties.ElasticsearchProperties;
import io.extremum.starter.CommonConfiguration;
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
            ElasticsearchDescriptorFacilities descriptorFacilities,
            ObjectMapper objectMapper) {
        return new ClassicTestElasticsearchModelDao(elasticsearchProperties, descriptorService,
                descriptorFacilities, objectMapper);
    }
}
