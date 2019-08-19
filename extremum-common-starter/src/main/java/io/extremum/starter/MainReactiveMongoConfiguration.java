package io.extremum.starter;

import com.mongodb.reactivestreams.client.MongoClients;
import io.extremum.starter.properties.MongoProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;

@Configuration
@EnableConfigurationProperties(MongoProperties.class)
@RequiredArgsConstructor
public class MainReactiveMongoConfiguration extends AbstractReactiveMongoConfiguration {
    private final MongoProperties mongoProperties;

    @Override
    public com.mongodb.reactivestreams.client.MongoClient reactiveMongoClient() {
        return MongoClients.create(mongoProperties.getServiceDbUri());
    }

    @Override
    protected String getDatabaseName() {
        return mongoProperties.getServiceDbName();
    }
}
