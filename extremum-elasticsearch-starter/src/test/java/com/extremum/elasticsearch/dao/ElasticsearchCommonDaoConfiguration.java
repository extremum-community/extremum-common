package com.extremum.elasticsearch.dao;

import com.extremum.elasticsearch.config.ElasticsearchRepositoriesConfiguration;
import com.extremum.starter.CommonConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author rpuch
 */
@Configuration
@Import({CommonConfiguration.class, ElasticsearchRepositoriesConfiguration.class})
public class ElasticsearchCommonDaoConfiguration {
}
