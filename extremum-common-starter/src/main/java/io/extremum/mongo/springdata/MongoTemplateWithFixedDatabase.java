package io.extremum.mongo.springdata;

import com.mongodb.client.MongoDatabase;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoDatabaseUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;

import static org.springframework.data.mongodb.SessionSynchronization.*;

/**
 * @author rpuch
 */
public class MongoTemplateWithFixedDatabase extends MongoTemplate {
    private final MongoDatabaseFactory mongoDatabaseFactory;
    private final String databaseName;

    public MongoTemplateWithFixedDatabase(MongoDatabaseFactory mongoDatabaseFactory,
            MappingMongoConverter mappingMongoConverter, String databaseName) {
        super(mongoDatabaseFactory, mappingMongoConverter);
        this.mongoDatabaseFactory = mongoDatabaseFactory;
        this.databaseName = databaseName;
    }

    @Override
    protected MongoDatabase doGetDatabase() {
        return MongoDatabaseUtils.getDatabase(databaseName, mongoDatabaseFactory, ON_ACTUAL_TRANSACTION);
    }
}
