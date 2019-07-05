package descriptor;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.factory.DescriptorSaver;
import com.extremum.common.test.TestWithServices;
import com.mongodb.client.model.Filters;
import config.DescriptorConfiguration;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoOperations;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static descriptor.DocumentByNameMatcher.havingName;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author rpuch
 */
@SpringBootTest(classes = DescriptorConfiguration.class)
class DescriptorMongoStorageTest extends TestWithServices {
    private static final String EXPECTED_DESCRIPTOR_COLLECTION = "descriptor-identifiers";

    @Autowired
    private DescriptorSaver descriptorSaver;
    @Autowired
    private MongoOperations mongoOperations;

    @Test
    void whenDescriptorIsStored_thenItShouldBeStoredInCollectionNamedDescriptorIdentifiers() {
        Descriptor descriptor = createAndSaveNewDescriptor();

        assertTrue(mongoOperations.collectionExists(EXPECTED_DESCRIPTOR_COLLECTION),
                "Expected collection does not exist");
        assertFalse(mongoOperations.collectionExists("descriptor"), "Wrong collection exists");

        Document document = findDescriptorDocument(descriptor);
        assertThat(document.get("_id"), is(descriptor.getExternalId()));
    }

    private Document findDescriptorDocument(Descriptor descriptor) {
        List<Document> documents = mongoOperations.getCollection(EXPECTED_DESCRIPTOR_COLLECTION)
                .find(Filters.eq("_id", descriptor.getExternalId()))
                .into(new ArrayList<>());

        assertThat(documents, hasSize(1));

        return documents.get(0);
    }

    private Descriptor createAndSaveNewDescriptor() {
        return descriptorSaver.createAndSave(new ObjectId().toString(), "Test",
                Descriptor.StorageType.MONGO);
    }

    @Test
    void whenDescriptorIsStored_thenCreatedAndModifiedAndVersionShouldBeSet() {
        Descriptor descriptor = createAndSaveNewDescriptor();

        Document document = findDescriptorDocument(descriptor);

        assertThat(document.get("created"), is(instanceOf(Date.class)));
        assertThat(document.get("modified"), is(instanceOf(Date.class)));
        assertThat(document.getLong("version"), is(0L));
    }

    @Test
    void makeSureAnIndexIsCreatedForDescriptorInternalId() {
        ArrayList<Document> indices = mongoOperations.getCollection(EXPECTED_DESCRIPTOR_COLLECTION)
                .listIndexes()
                .into(new ArrayList<>());

        assertThat(indices, hasItem(havingName("internalId")));
    }

}
