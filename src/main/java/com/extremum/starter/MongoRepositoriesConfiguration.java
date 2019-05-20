package com.extremum.starter;

import com.extremum.common.repository.mongo.EnableExtremumMongoRepositories;
import com.extremum.common.repository.mongo.SoftDeleteMongoRepository;
import com.extremum.common.repository.mongo.SoftDeleteMongoRepositoryFactoryBean;
import com.extremum.starter.properties.MongoProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * @author rpuch
 */
@Configuration
@EnableExtremumMongoRepositories(basePackages = "${mongo.repository-packages}",
        repositoryBaseClass = SoftDeleteMongoRepository.class,
        repositoryFactoryBeanClass = SoftDeleteMongoRepositoryFactoryBean.class)
@ConditionalOnProperty(MongoProperties.REPOSITORY_PACKAGES_PROPERTY)
@RequiredArgsConstructor
public class MongoRepositoriesConfiguration {
}
