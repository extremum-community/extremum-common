package io.extremum.mongo.config;

import io.extremum.common.secondaryds.SecondaryDatasource;
import io.extremum.mongo.properties.MongoProperties;
import io.extremum.mongo.springdata.repository.EnableExtremumMongoRepositories;
import io.extremum.mongo.springdata.reactiverepository.EnableExtremumReactiveMongoRepositories;
import io.extremum.mongo.springdata.repository.ExtremumMongoRepositoryFactoryBean;
import io.extremum.mongo.springdata.reactiverepository.ExtremumReactiveMongoRepositoryFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author rpuch
 */
@Configuration
@EnableExtremumMongoRepositories(basePackages = "${mongo.repository-packages}",
        repositoryFactoryBeanClass = ExtremumMongoRepositoryFactoryBean.class,
        excludeFilters = @ComponentScan.Filter(SecondaryDatasource.class))
@EnableExtremumReactiveMongoRepositories(basePackages = "${mongo.repository-packages}",
        repositoryFactoryBeanClass = ExtremumReactiveMongoRepositoryFactoryBean.class,
        excludeFilters = @ComponentScan.Filter(SecondaryDatasource.class))
@ConditionalOnProperty(MongoProperties.REPOSITORY_PACKAGES_PROPERTY)
public class MongoRepositoriesConfiguration {
}
