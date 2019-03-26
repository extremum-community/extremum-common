package com.extremum.common.config;

import com.extremum.common.converter.ZonedDateTimeConverter;
import com.extremum.common.dao.TestModelDao;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableConfigurationProperties(MongoProperties.class)
public class AppConfiguration {

    @Autowired
    private MongoProperties mongoProps;

    @Bean
    public Datastore datastore() {
        Morphia morphia = new Morphia();
        morphia.getMapper().getConverters().addConverter(ZonedDateTimeConverter.class);
        MongoClientURI databaseUri = new MongoClientURI(mongoProps.getUri());
        MongoClient mongoClient = new MongoClient(databaseUri);
        Datastore datastore = morphia.createDatastore(mongoClient, mongoProps.getDbName());

        datastore.ensureIndexes();

        return datastore;
    }

    @Bean
    public TestModelDao testModelDao(Datastore datastore) {
        return new TestModelDao(datastore);
    }
}
