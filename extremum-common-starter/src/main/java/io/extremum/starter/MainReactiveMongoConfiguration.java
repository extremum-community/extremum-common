package io.extremum.starter;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import io.extremum.starter.properties.MongoProperties;
import lombok.RequiredArgsConstructor;
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
    public ReactiveMongoOperations reactiveMongoTemplate(MappingMongoConverter mappingMongoConverter) {
        return new ReactiveMongoTemplate(reactiveMongoDbFactory(), mappingMongoConverter);
    }

    @Bean
    public ReactiveMongoDatabaseFactory reactiveMongoDbFactory() {
        return new SimpleReactiveMongoDatabaseFactory(reactiveMongoClient(), getDatabaseName());
    }
}
