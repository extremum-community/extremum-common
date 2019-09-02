package io.extremum.mongo.config;

import io.extremum.mongo.properties.MongoProperties;
import io.extremum.mongo.repository.EnableExtremumMongoRepositories;
import io.extremum.mongo.repository.EnableExtremumReactiveMongoRepositories;
import io.extremum.mongo.repository.ExtremumMongoRepositoryFactoryBean;
import io.extremum.mongo.repository.ExtremumReactiveMongoRepositoryFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * @author rpuch
 */
@Configuration
@EnableExtremumMongoRepositories(basePackages = "${mongo.repository-packages}",
        repositoryFactoryBeanClass = ExtremumMongoRepositoryFactoryBean.class)
@EnableExtremumReactiveMongoRepositories(basePackages = "${mongo.repository-packages}",
        repositoryFactoryBeanClass = ExtremumReactiveMongoRepositoryFactoryBean.class)
@ConditionalOnProperty(MongoProperties.REPOSITORY_PACKAGES_PROPERTY)
public class MongoRepositoriesConfiguration {
}
