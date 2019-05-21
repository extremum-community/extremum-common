package descriptor;

import com.extremum.common.collection.CollectionDescriptor;
import com.extremum.common.collection.OwnedCoordinates;
import com.extremum.common.collection.dao.CollectionDescriptorDao;
import com.extremum.common.collection.dao.impl.CollectionDescriptorRepository;
import com.extremum.common.collection.service.CollectionDescriptorService;
import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.factory.impl.MongoDescriptorFactory;
import com.extremum.common.test.TestWithServices;
import common.dao.mongo.MongoCommonDaoConfiguration;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;


@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = MongoCommonDaoConfiguration.class)
public class MongoCollectionDescriptorDaoTest extends TestWithServices {
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

    @Test
    public void givenACollectionDescriptorExists_whenItIsSearchedFor_thenItShouldBeFound() {
        String hostExternalId = createADescriptor();
        CollectionDescriptor descriptor = createACollectionDescriptor(hostExternalId);

        Optional<CollectionDescriptor> optDescriptor = collectionDescriptorRepository.findByExternalId(
                descriptor.getExternalId());
        assertThat(optDescriptor.isPresent(), is(true));
    }

    @Test
    public void givenACollectionDescriptorIsSoftDeleted_whenItIsSearchedFor_thenItShouldNotBeFound() {
        String hostExternalId = createADescriptor();
        CollectionDescriptor descriptor = createACollectionDescriptor(hostExternalId);

        descriptor.setDeleted(true);
        collectionDescriptorRepository.save(descriptor);

        Optional<CollectionDescriptor> optDescriptor = collectionDescriptorRepository.findByExternalId(
                descriptor.getExternalId());
        assertThat(optDescriptor.isPresent(), is(false));
    }
}
