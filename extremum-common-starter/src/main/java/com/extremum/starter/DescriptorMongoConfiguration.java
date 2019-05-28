package com.extremum.starter;

import com.extremum.common.collection.dao.impl.CollectionDescriptorRepository;
import com.extremum.common.collection.spring.CollectionDescriptorLifecycleListener;
import com.extremum.common.descriptor.dao.impl.DescriptorRepository;
import com.extremum.common.repository.mongo.SoftDeleteMongoRepositoryFactoryBean;
import com.extremum.starter.properties.MongoProperties;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.ArrayList;
import java.util.List;

/**
 * @author rpuch
 */
@Configuration
@EnableConfigurationProperties(MongoProperties.class)
@EnableMongoRepositories(basePackageClasses = {DescriptorRepository.class, CollectionDescriptorRepository.class},
        repositoryFactoryBeanClass = SoftDeleteMongoRepositoryFactoryBean.class)
@EnableMongoAuditing(dateTimeProviderRef = "dateTimeProvider")
@RequiredArgsConstructor
public class DescriptorMongoConfiguration extends AbstractMongoConfiguration {
    private final MongoProperties mongoProps;
    private final MongoClientURI databaseUri;

    @Override
    @Bean
    public MongoClient mongoClient() {
        return new MongoClient(databaseUri);
    }

    @Override
    protected String getDatabaseName() {
        return mongoProps.getDbName();
    }

    @Bean
    public MongoCustomConversions customConversions() {
        List<Object> converters = new ArrayList<>();

        converters.add(new DateToZonedDateTimeConverter());
        converters.add(new ZonedDateTimeToDateConverter());
        converters.add(new DescriptorToStringConverter());

        return new MongoCustomConversions(converters);
    }

    @Bean
    public CollectionDescriptorLifecycleListener collectionDescriptorLifecycleListener() {
        return new CollectionDescriptorLifecycleListener();
    }
}
