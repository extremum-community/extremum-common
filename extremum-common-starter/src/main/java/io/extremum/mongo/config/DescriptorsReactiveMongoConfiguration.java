package io.extremum.mongo.config;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import io.extremum.mongo.properties.MongoProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
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
}
