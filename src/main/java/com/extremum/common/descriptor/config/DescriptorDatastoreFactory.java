package com.extremum.common.descriptor.config;

import com.extremum.common.converters.MongoZonedDateTimeConverter;
import com.extremum.common.descriptor.serde.mongo.DescriptorStringConverter;
import com.extremum.starter.properties.MongoProperties;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

public class DescriptorDatastoreFactory {
    public Datastore createDescriptorDatastore(MongoProperties mongoProperties) {
        Morphia morphia = new Morphia();
        morphia.getMapper().getOptions().setStoreEmpties(true);
        morphia.getMapper().getConverters().addConverter(MongoZonedDateTimeConverter.class);
        morphia.getMapper().getConverters().addConverter(DescriptorStringConverter.class);
        MongoClientURI databaseUri = new MongoClientURI(mongoProperties.getUri());
        MongoClient mongoClient = new MongoClient(databaseUri);
        Datastore datastore = morphia.createDatastore(mongoClient, mongoProperties.getDbName());
        datastore.ensureIndexes();
        return datastore;
    }
}
