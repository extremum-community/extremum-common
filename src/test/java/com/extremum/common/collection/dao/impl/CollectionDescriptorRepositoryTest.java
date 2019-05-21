package com.extremum.common.collection.dao.impl;

import com.extremum.common.collection.CollectionDescriptor;
import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.service.DescriptorService;
import com.extremum.common.test.TestWithServices;
import com.extremum.starter.properties.MongoProperties;
import com.mongodb.MongoClient;
import com.mongodb.client.model.Filters;
import common.dao.mongo.MongoCommonDaoConfiguration;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author rpuch
 */
@ExtendWith(SpringExtension.class)
@DataMongoTest
@ContextConfiguration(classes = MongoCommonDaoConfiguration.class)
public class CollectionDescriptorRepositoryTest extends TestWithServices {
    @Autowired
    private CollectionDescriptorRepository repository;

    @Autowired
    private MongoClient mongoClient;
    @Autowired
    private MongoProperties mongoProperties;

    @Test
    public void whenCollectionDescriptorIsSaved_thenANewDocumentShouldAppearInMongo() {
        String internalId = new ObjectId().toString();
        Descriptor hostId = Descriptor.builder()
                .externalId(DescriptorService.createExternalId())
                .internalId(internalId)
                .modelType("test_model")
                .storageType(Descriptor.StorageType.MONGO)
                .build();
        DescriptorService.store(hostId);

        CollectionDescriptor collectionDescriptor = CollectionDescriptor.forOwned(hostId, "the-field");

        repository.save(collectionDescriptor);
        assertThat(collectionDescriptor.getExternalId(), is(notNullValue()));

        List<Document> documents = mongoClient.getDatabase(mongoProperties.getDbName())
                .getCollection("collection-descriptors")
                .find(Filters.eq("_id", collectionDescriptor.getExternalId()), Document.class)
                .into(new ArrayList<>());

        assertThat(documents, hasSize(1));
        Document document = documents.get(0);

        assertThat(document.getString("_id"), is(equalTo(collectionDescriptor.getExternalId())));
        assertThat(document.getString("type"), is("OWNED"));
        Document coordinates = document.get("coordinates", Document.class);
        assertThat(coordinates, is(notNullValue()));
        Document ownedCoordinates = coordinates.get("ownedCoordinates", Document.class);
        assertThat(ownedCoordinates, is(notNullValue()));
        assertThat(ownedCoordinates.getString("hostId"), is(equalTo(hostId.getExternalId())));
        assertThat(ownedCoordinates.getString("hostFieldName"), is("the-field"));
        assertThat(document.getString("coordinatesString"), startsWith("OWNED/"));
        assertThat(document.get("created", Date.class), is(notNullValue()));
        assertThat(document.getBoolean("deleted"), is(false));
    }
}