package descriptor;

import com.extremum.common.collection.CollectionDescriptor;
import com.extremum.common.collection.OwnedCoordinates;
import com.extremum.common.collection.dao.CollectionDescriptorDao;
import com.extremum.common.collection.dao.impl.CollectionDescriptorRepository;
import com.extremum.common.collection.service.CollectionDescriptorService;
import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.factory.impl.MongoDescriptorFactory;
import config.DescriptorConfiguration;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
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
@SpringBootTest(classes = DescriptorConfiguration.class)
public class MongoCollectionDescriptorDaoTest {
    @Autowired
    private CollectionDescriptorDao collectionDescriptorDao;

    @Autowired
    private CollectionDescriptorService collectionDescriptorService;

    @Autowired
    private CollectionDescriptorRepository collectionDescriptorRepository;

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
        assertThat(retrievedDescriptor.get().getType(), is(CollectionDescriptor.Type.OWNED));
        OwnedCoordinates ownedCoordinates = retrievedDescriptor.get().getCoordinates().getOwnedCoordinates();
        assertThat(ownedCoordinates.getHostId().getExternalId(), is(hostExternalId));
        assertThat(ownedCoordinates.getHostFieldName(), is("items"));
    }

    @NotNull
    private CollectionDescriptor createACollectionDescriptor(String hostExternalId) {
        CollectionDescriptor collectionDescriptor = CollectionDescriptor.forOwned(
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
        CollectionDescriptor descriptor = CollectionDescriptor.forOwned(new Descriptor(hostExternalId), "items");
        String coordinatesString = descriptor.toCoordinatesString();

        Optional<CollectionDescriptor> retrievedDescriptor = collectionDescriptorDao.retrieveByCoordinates(
                coordinatesString);
        assertFalse(retrievedDescriptor.isPresent());

        collectionDescriptorRepository.save(descriptor);
        retrievedDescriptor = collectionDescriptorDao.retrieveByCoordinates(coordinatesString);
        assertTrue(retrievedDescriptor.isPresent());
        assertEquals(descriptor, retrievedDescriptor.get());
    }
}
