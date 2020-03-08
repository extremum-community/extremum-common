package io.extremum.mongo.config;

import com.mongodb.WriteConcern;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import io.extremum.common.descriptor.dao.ReactiveDescriptorDao;
import io.extremum.common.descriptor.factory.DescriptorFactory;
import io.extremum.common.descriptor.factory.ReactiveDescriptorSaver;
import io.extremum.common.reactive.ReactiveEventPublisher;
import io.extremum.mongo.facilities.ReactiveMongoDescriptorFacilities;
import io.extremum.mongo.facilities.ReactiveMongoDescriptorFacilitiesImpl;
import io.extremum.mongo.properties.MongoProperties;
import io.extremum.mongo.service.lifecycle.ReactiveMongoCommonModelLifecycleListener;
import io.extremum.mongo.service.lifecycle.ReactiveMongoVersionedModelLifecycleListener;
import io.extremum.mongo.springdata.ReactiveMongoTemplateWithReactiveEvents;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.ReactiveMongoTransactionManager;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.SimpleReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.core.WriteResultChecking;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableConfigurationProperties(MongoProperties.class)
@EnableTransactionManagement
@RequiredArgsConstructor
public class MainReactiveMongoConfiguration {
    private final MongoProperties mongoProperties;

    @Bean
    public MongoClient reactiveMongoClient() {
        return MongoClients.create(mongoProperties.getServiceDbUri());
    }

    private String getDatabaseName() {
        return mongoProperties.getServiceDbName();
    }

    @Bean
    @Primary
    public ReactiveMongoOperations reactiveMongoTemplate(MappingMongoConverter mappingMongoConverter,
                                                         ReactiveEventPublisher reactiveEventPublisher) {
        ReactiveMongoTemplateWithReactiveEvents template = new ReactiveMongoTemplateWithReactiveEvents(
                reactiveMongoDbFactory(), mappingMongoConverter, reactiveEventPublisher);
        template.setWriteResultChecking(WriteResultChecking.EXCEPTION);
        template.setWriteConcern(WriteConcern.MAJORITY);
        return template;
    }

    @Bean
    public ReactiveMongoDatabaseFactory reactiveMongoDbFactory() {
        return new SimpleReactiveMongoDatabaseFactory(reactiveMongoClient(), getDatabaseName());
    }

    @Bean
    public ReactiveMongoTransactionManager reactiveMongoTransactionManager() {
        return new ReactiveMongoTransactionManager(reactiveMongoDbFactory());
    }

    @Bean
    @ConditionalOnMissingBean
    public ReactiveMongoDescriptorFacilities reactiveMongoDescriptorFacilities(
            DescriptorFactory descriptorFactory, ReactiveDescriptorSaver reactiveDescriptorSaver,
            ReactiveDescriptorDao reactiveDescriptorDao) {
        return new ReactiveMongoDescriptorFacilitiesImpl(descriptorFactory, reactiveDescriptorSaver,
                reactiveDescriptorDao);
    }

    @Bean
    public ReactiveMongoCommonModelLifecycleListener reactiveMongoCommonModelLifecycleListener(
            ReactiveMongoDescriptorFacilities reactiveMongoDescriptorFacilities) {
        return new ReactiveMongoCommonModelLifecycleListener(reactiveMongoDescriptorFacilities);
    }

    @Bean
    public ReactiveMongoVersionedModelLifecycleListener reactiveMongoVersionedModelLifecycleListener(
            ReactiveMongoDescriptorFacilities reactiveMongoDescriptorFacilities) {
        return new ReactiveMongoVersionedModelLifecycleListener(reactiveMongoDescriptorFacilities);
    }
}
