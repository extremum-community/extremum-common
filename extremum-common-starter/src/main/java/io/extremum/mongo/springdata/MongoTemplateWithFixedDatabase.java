package io.extremum.mongo.springdata;

import com.mongodb.client.MongoDatabase;
import org.springframework.data.mongodb.MongoDatabaseUtils;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;

import static org.springframework.data.mongodb.SessionSynchronization.*;

/**
 * @author rpuch
 */
public class MongoTemplateWithFixedDatabase extends MongoTemplate {
    private final MongoDbFactory mongoDbFactory;
    private final String databaseName;

    public MongoTemplateWithFixedDatabase(MongoDbFactory mongoDbFactory,
            MappingMongoConverter mappingMongoConverter, String databaseName) {
        super(mongoDbFactory, mappingMongoConverter);
        this.mongoDbFactory = mongoDbFactory;
        this.databaseName = databaseName;
    }

    @Override
    protected MongoDatabase doGetDatabase() {
        return MongoDatabaseUtils.getDatabase(databaseName, mongoDbFactory, ON_ACTUAL_TRANSACTION);
    }
}
