package io.extremum.mongo.config;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import io.extremum.common.descriptor.factory.DescriptorFactory;
import io.extremum.common.descriptor.factory.ReactiveDescriptorSaver;
import io.extremum.mongo.facilities.ReactiveMongoDescriptorFacilities;
import io.extremum.mongo.facilities.ReactiveMongoDescriptorFacilitiesImpl;
import io.extremum.mongo.properties.MongoProperties;
import io.extremum.mongo.service.lifecycle.ReactiveMongoCommonModelLifecycleListener;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.SimpleReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;

@Configuration
@EnableConfigurationProperties(MongoProperties.class)
@RequiredArgsConstructor
public class DescriptorsReactiveMongoConfiguration {
    private final MongoProperties mongoProperties;

    @Bean
    public MongoClient descriptorsReactiveMongoClient() {
        return MongoClients.create(mongoProperties.getDescriptorsDbUri());
    }

    private String getDatabaseName() {
        return mongoProperties.getDescriptorsDbName();
    }

    @Bean
    public ReactiveMongoOperations descriptorsReactiveMongoTemplate(
            @Qualifier("descriptorsMappingMongoConverter") MappingMongoConverter mappingMongoConverter) {
        return new ReactiveMongoTemplate(descriptorsReactiveMongoDbFactory(), mappingMongoConverter);
    }

    @Bean
    public ReactiveMongoDatabaseFactory descriptorsReactiveMongoDbFactory() {
        return new SimpleReactiveMongoDatabaseFactory(descriptorsReactiveMongoClient(), getDatabaseName());
    }

    @Bean
    @ConditionalOnMissingBean
    public ReactiveMongoDescriptorFacilities reactiveMongoDescriptorFacilities(
            DescriptorFactory descriptorFactory, ReactiveDescriptorSaver reactiveDescriptorSaver) {
        return new ReactiveMongoDescriptorFacilitiesImpl(descriptorFactory, reactiveDescriptorSaver);
    }

    @Bean
    public ReactiveMongoCommonModelLifecycleListener reactiveMongoCommonModelLifecycleListener(
            ReactiveMongoDescriptorFacilities reactiveMongoDescriptorFacilities) {
        return new ReactiveMongoCommonModelLifecycleListener(reactiveMongoDescriptorFacilities);
    }
}
