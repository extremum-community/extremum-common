package io.extremum.common.collection.dao.impl;

import com.mongodb.MongoClient;
import com.mongodb.client.model.Filters;
import common.dao.mongo.MongoCommonDaoConfiguration;
import io.extremum.common.collection.CollectionDescriptor;
import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.common.test.TestWithServices;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.starter.properties.MongoProperties;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoReactiveRepositoriesAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

/**
 * @author rpuch
 */
@SpringBootTest(classes = MongoCommonDaoConfiguration.class)
@ImportAutoConfiguration(exclude = MongoReactiveRepositoriesAutoConfiguration.class)
class CollectionDescriptorRepositoryTest extends TestWithServices {
    @Autowired
    private CollectionDescriptorRepository repository;

    @Autowired
    private DescriptorService descriptorService;
    @Autowired
    @Qualifier("descriptorsMongoClient")
    private MongoClient mongoClient;
    @Autowired
    private MongoProperties mongoProperties;

    @Test
    void whenCollectionDescriptorIsSaved_thenANewDocumentShouldAppearInMongo() {
        String internalId = new ObjectId().toString();
        Descriptor hostId = Descriptor.builder()
                .externalId(descriptorService.createExternalId())
                .internalId(internalId)
                .modelType("test_model")
                .storageType(Descriptor.StorageType.MONGO)
                .build();
        descriptorService.store(hostId);

        CollectionDescriptor collectionDescriptor = CollectionDescriptor.forOwned(hostId, "the-field");

        repository.save(collectionDescriptor);
        assertThat(collectionDescriptor.getExternalId(), is(notNullValue()));

        List<Document> documents = mongoClient.getDatabase(mongoProperties.getDescriptorsDbName())
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
        assertThat(ownedCoordinates.getString("hostAttributeName"), is("the-field"));
        assertThat(document.getString("coordinatesString"), startsWith("OWNED/"));
        assertThat(document.get("created", Date.class), is(notNullValue()));
        assertThat(document.getBoolean("deleted"), is(false));
    }
}