package io.extremum.mongo.config;

import com.mongodb.WriteConcern;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import io.extremum.mongo.properties.MongoProperties;
import io.extremum.mongo.springdata.DescriptorsMongoDb;
import io.extremum.mongo.springdata.MainMongoDb;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.ReactiveMongoDatabaseUtils;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.WriteResultChecking;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;

import static org.springframework.data.mongodb.SessionSynchronization.*;

@Configuration
@EnableConfigurationProperties(MongoProperties.class)
@RequiredArgsConstructor
public class DescriptorsReactiveMongoConfiguration {
    private final MongoProperties mongoProperties;

    private String getDatabaseName() {
        return mongoProperties.getDescriptorsDbName();
    }

    @Bean
    @DescriptorsMongoDb
    public ReactiveMongoOperations descriptorsReactiveMongoTemplate(
            @MainMongoDb ReactiveMongoDatabaseFactory mainReactiveMongoDatabaseFactory,
            @DescriptorsMongoDb MappingMongoConverter mappingMongoConverter) {
        ReactiveMongoTemplate template = new ReactiveMongoTemplate(mainReactiveMongoDatabaseFactory,
                mappingMongoConverter) {
            // Method redefinition is required to make sure we use the correct database.
            // Currently, spring-data-mongodb uses ReactiveMongoDatabaseFactory as a transactional resource, so we
            // must have only one such factory, but by default ReactiveMongoTemplate uses the database configured
            // on the factory. That's why we have to specifically switch to the needed database.

            @Override
            protected Mono<MongoDatabase> doGetDatabase() {
                return ReactiveMongoDatabaseUtils.getDatabase(getDatabaseName(), mainReactiveMongoDatabaseFactory,
                        ON_ACTUAL_TRANSACTION);
            }

            @Override
            public MongoDatabase getMongoDatabase() {
                return mainReactiveMongoDatabaseFactory.getMongoDatabase(getDatabaseName());
            }

            @Override
            public MongoCollection<Document> getCollection(String collectionName) {
                Assert.notNull(collectionName, "Collection name must not be null!");

                try {
                    return mainReactiveMongoDatabaseFactory.getMongoDatabase(getDatabaseName())
                            .getCollection(collectionName);
                } catch (RuntimeException e) {
                    throw potentiallyConvertRuntimeException(e,
                            mainReactiveMongoDatabaseFactory.getExceptionTranslator());
                }
            }

            private RuntimeException potentiallyConvertRuntimeException(RuntimeException ex,
           			PersistenceExceptionTranslator exceptionTranslator) {
           		RuntimeException resolved = exceptionTranslator.translateExceptionIfPossible(ex);
           		return resolved == null ? ex : resolved;
           	}
        };
        template.setWriteResultChecking(WriteResultChecking.EXCEPTION);
        template.setWriteConcern(WriteConcern.MAJORITY);
        return template;
    }
}
