package com.extremum.common.descriptor.dao.impl;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.service.DescriptorService;
import com.extremum.common.test.TestWithServices;
import com.extremum.starter.properties.MongoProperties;
import com.mongodb.MongoClient;
import com.mongodb.client.model.Updates;
import common.dao.mongo.MongoCommonDaoConfiguration;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.mongodb.client.model.Filters.eq;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

/**
 * @author rpuch
 */
@RunWith(SpringRunner.class)
@DataMongoTest
@ContextConfiguration(classes = MongoCommonDaoConfiguration.class)
public class DescriptorRepositoryTest extends TestWithServices {
    @Autowired
    private DescriptorRepository descriptorRepository;

    @Autowired
    private MongoClient mongoClient;
    @Autowired
    private MongoProperties mongoProperties;

    @Test
    public void whenDescriptorIsSaved_thenANewDocumentShouldAppearInMongo() {
        String internalId = new ObjectId().toString();
        Descriptor descriptor = newDescriptor(internalId);

        descriptorRepository.save(descriptor);

        List<Document> documents = mongoClient.getDatabase(mongoProperties.getDbName())
                .getCollection("descriptor-identifiers")
                .find(eq("_id", descriptor.getExternalId()), Document.class)
                .into(new ArrayList<>());

        assertThatDocumentWith1DescriptorWasReturned(documents, descriptor);
    }

    private Descriptor newDescriptor(String internalId) {
        return Descriptor.builder()
                .externalId(DescriptorService.createExternalId())
                .internalId(internalId)
                .modelType("test_model")
                .storageType(Descriptor.StorageType.MONGO)
                .build();
    }

    private void assertThatDocumentWith1DescriptorWasReturned(List<Document> documents,
                                                              Descriptor expectedDescriptor) {
        assertThat(documents, hasSize(1));

        Document document = documents.get(0);

        assertThat(document.getString("_id"), is(equalTo(expectedDescriptor.getExternalId())));
        assertThat(document.getString("internalId"), is(equalTo(expectedDescriptor.getInternalId())));
        assertThat(document.getString("modelType"), is("test_model"));
        assertThat(document.getString("storageType"), is("MONGO"));
        assertThat(document.get("created", Date.class), is(notNullValue()));
        assertThat(document.get("modified", Date.class), is(notNullValue()));
        assertThat(document.getBoolean("deleted"), is(false));
        assertThat(document.getLong("version"), is(0L));
    }

    @Test
    public void whenDescriptorClassInMongoIsNotAvailable_thenDescriptorShouldBeLoadedSuccessfully() {
        String internalId = new ObjectId().toString();
        Descriptor descriptor = newDescriptor(internalId);

        descriptorRepository.save(descriptor);

        mongoClient.getDatabase(mongoProperties.getDbName())
                .getCollection("descriptor-identifiers")
                .updateOne(
                        eq("_id", descriptor.getExternalId()),
                        Updates.set("_class", "no.such.class.AtAll")
                );

        Optional<Descriptor> retrievedDescriptorOpt = descriptorRepository.findById(descriptor.getExternalId());

        assertThatADescriptorWasFound(retrievedDescriptorOpt, descriptor);
    }

    @SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "OptionalGetWithoutIsPresent"})
    private void assertThatADescriptorWasFound(Optional<Descriptor> retrievedDescriptorOpt,
                                               Descriptor expectedDescriptor) {
        assertThat(retrievedDescriptorOpt.isPresent(), is(true));

        Descriptor retrievedDescriptor = retrievedDescriptorOpt.get();

        assertThat(retrievedDescriptor.getExternalId(), is(equalTo(expectedDescriptor.getExternalId())));
        assertThat(retrievedDescriptor.getInternalId(), is(equalTo(expectedDescriptor.getInternalId())));
    }
}