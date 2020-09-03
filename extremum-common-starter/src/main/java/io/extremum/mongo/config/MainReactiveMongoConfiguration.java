package io.extremum.mongo.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.WriteConcern;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import io.extremum.common.descriptor.factory.DescriptorFactory;
import io.extremum.common.descriptor.factory.ReactiveDescriptorSaver;
import io.extremum.mongo.facilities.ReactiveMongoDescriptorFacilities;
import io.extremum.mongo.facilities.ReactiveMongoDescriptorFacilitiesImpl;
import io.extremum.mongo.properties.MongoProperties;
import io.extremum.mongo.service.lifecycle.ReactiveMongoCommonModelLifecycleCallbacks;
import io.extremum.mongo.service.lifecycle.ReactiveMongoVersionedModelLifecycleCallbacks;
import io.extremum.mongo.springdata.MainMongoDb;
import lombok.RequiredArgsConstructor;
import org.bson.UuidRepresentation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.ReactiveMongoTransactionManager;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
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
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(mongoProperties.getUri()))
                .uuidRepresentation(UuidRepresentation.STANDARD)
                .build();
        return MongoClients.create(settings);
    }

    private String getDatabaseName() {
        return mongoProperties.getServiceDbName();
    }

    @Bean
    @Primary
    @MainMongoDb
    public ReactiveMongoOperations reactiveMongoTemplate(MappingMongoConverter mappingMongoConverter) {
        ReactiveMongoTemplate template = new ReactiveMongoTemplate(reactiveMongoDbFactory(), mappingMongoConverter);
        template.setWriteResultChecking(WriteResultChecking.EXCEPTION);
        template.setWriteConcern(WriteConcern.MAJORITY);
        return template;
    }

    @Bean
    @MainMongoDb
    public ReactiveMongoDatabaseFactory reactiveMongoDbFactory() {
        return new SimpleReactiveMongoDatabaseFactory(reactiveMongoClient(), getDatabaseName());
    }

    @Bean
    @MainMongoDb
    public ReactiveMongoTransactionManager reactiveMongoTransactionManager() {
        return new ReactiveMongoTransactionManager(reactiveMongoDbFactory());
    }

    @Bean
    @ConditionalOnMissingBean
    public ReactiveMongoDescriptorFacilities reactiveMongoDescriptorFacilities(
            DescriptorFactory descriptorFactory, ReactiveDescriptorSaver reactiveDescriptorSaver) {
        return new ReactiveMongoDescriptorFacilitiesImpl(descriptorFactory, reactiveDescriptorSaver);
    }

    @Bean
    public ReactiveMongoCommonModelLifecycleCallbacks reactiveMongoCommonModelLifecycleCallbacks(
            ReactiveMongoDescriptorFacilities reactiveMongoDescriptorFacilities) {
        return new ReactiveMongoCommonModelLifecycleCallbacks(reactiveMongoDescriptorFacilities);
    }

    @Bean
    public ReactiveMongoVersionedModelLifecycleCallbacks reactiveMongoVersionedModelLifecycleCallbacks(
            ReactiveMongoDescriptorFacilities reactiveMongoDescriptorFacilities) {
        return new ReactiveMongoVersionedModelLifecycleCallbacks(reactiveMongoDescriptorFacilities);
    }
}
