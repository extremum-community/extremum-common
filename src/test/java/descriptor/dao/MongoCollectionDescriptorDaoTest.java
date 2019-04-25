package descriptor.dao;

import com.extremum.common.collection.CollectionDescriptor;
import com.extremum.common.collection.EmbeddedCoordinates;
import com.extremum.common.collection.dao.CollectionDescriptorDao;
import com.extremum.common.collection.service.CollectionDescriptorService;
import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.factory.impl.MongoDescriptorFactory;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mongodb.morphia.Datastore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


@RunWith(SpringRunner.class)
@SpringBootTest
public class MongoCollectionDescriptorDaoTest {
    @Autowired
    private CollectionDescriptorDao collectionDescriptorDao;

    @Autowired
    private CollectionDescriptorService collectionDescriptorService;

    @Autowired
    private Datastore descriptorsStore;

    @Test
    public void testRetrieveByExternalId() {
        String hostExternalId = createADescriptor();
        CollectionDescriptor collectionDescriptor = createACollectionDescriptor(hostExternalId);

        Optional<CollectionDescriptor> retrievedDescriptor = collectionDescriptorDao.retrieveByExternalId(
                collectionDescriptor.getExternalId());

        assertThatCollectionDescriptorRetrievalWasOk(hostExternalId, collectionDescriptor, retrievedDescriptor);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private void assertThatCollectionDescriptorRetrievalWasOk(String hostExternalId,
            CollectionDescriptor collectionDescriptor, Optional<CollectionDescriptor> retrievedDescriptor) {
        assertTrue(retrievedDescriptor.isPresent());
        assertThat(retrievedDescriptor.get().getExternalId(), is(collectionDescriptor.getExternalId()));
        assertThat(retrievedDescriptor.get().getType(), is(CollectionDescriptor.Type.EMBEDDED));
        EmbeddedCoordinates embeddedCoordinates = retrievedDescriptor.get().getCoordinates().getEmbeddedCoordinates();
        assertThat(embeddedCoordinates.getHostId().getExternalId(), is(hostExternalId));
        assertThat(embeddedCoordinates.getHostFieldName(), is("items"));
    }

    @NotNull
    private CollectionDescriptor createACollectionDescriptor(String hostExternalId) {
        CollectionDescriptor collectionDescriptor = CollectionDescriptor.forEmbedded(
                new Descriptor(hostExternalId), "items");

        collectionDescriptorService.store(collectionDescriptor);

        assertThat(collectionDescriptor.getExternalId(), is(notNullValue()));
        return collectionDescriptor;
    }

    private String createADescriptor() {
        ObjectId objectId = new ObjectId();
        Descriptor hostDescriptor = MongoDescriptorFactory.create(objectId, "test_model");
        return hostDescriptor.getExternalId();
    }

    @Test
    public void testRetrieveByInternalId() {
        String hostExternalId = createADescriptor();
        CollectionDescriptor collectionDescriptor = createACollectionDescriptor(hostExternalId);

        Optional<CollectionDescriptor> retrievedDescriptor = collectionDescriptorDao.retrieveByCoordinates(
                collectionDescriptor.toCoordinatesString());

        assertThatCollectionDescriptorRetrievalWasOk(hostExternalId, collectionDescriptor, retrievedDescriptor);
    }

    @Test
    public void testRetrieveFromMongo() {
        String hostExternalId = createADescriptor();
        CollectionDescriptor descriptor = CollectionDescriptor.forEmbedded(new Descriptor(hostExternalId), "items");
        String coordinatesString = descriptor.toCoordinatesString();

        Optional<CollectionDescriptor> retrievedDescriptor = collectionDescriptorDao.retrieveByCoordinates(
                coordinatesString);
        assertFalse(retrievedDescriptor.isPresent());

        descriptorsStore.save(descriptor);
        retrievedDescriptor = collectionDescriptorDao.retrieveByCoordinates(coordinatesString);
        assertTrue(retrievedDescriptor.isPresent());
        assertEquals(descriptor, retrievedDescriptor.get());
    }
}
