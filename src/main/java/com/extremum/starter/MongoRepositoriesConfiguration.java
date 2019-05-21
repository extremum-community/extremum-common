package com.extremum.starter;

import com.extremum.common.repository.mongo.EnableExtremumMongoRepositories;
import com.extremum.common.repository.mongo.ExtremumMongoRepositoryFactoryBean;
import com.extremum.starter.properties.MongoProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * @author rpuch
 */
@Configuration
@EnableExtremumMongoRepositories(basePackages = "${mongo.repository-packages}",
        repositoryFactoryBeanClass = ExtremumMongoRepositoryFactoryBean.class)
@ConditionalOnProperty(MongoProperties.REPOSITORY_PACKAGES_PROPERTY)
@RequiredArgsConstructor
public class MongoRepositoriesConfiguration {
}
