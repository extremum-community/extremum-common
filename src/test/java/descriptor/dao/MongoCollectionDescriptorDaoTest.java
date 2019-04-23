package descriptor.dao;

import com.extremum.common.collection.CollectionDescriptor;
import com.extremum.common.collection.EmbeddedCoordinates;
import com.extremum.common.collection.dao.CollectionDescriptorDao;
import com.extremum.common.collection.service.CollectionDescriptorService;
import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.factory.impl.MongoDescriptorFactory;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mongodb.morphia.Datastore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
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
        Descriptor hostDescriptor = createADescriptor();
        String hostExternalId = hostDescriptor.getExternalId();
        CollectionDescriptor collectionDescriptor = new CollectionDescriptor(new Descriptor(hostExternalId), "items");

        collectionDescriptorService.store(collectionDescriptor);

        assertThat(collectionDescriptor.getExternalId(), is(notNullValue()));

        Optional<CollectionDescriptor> retrievedDescriptor = collectionDescriptorDao.retrieveByExternalId(
                collectionDescriptor.getExternalId());
        assertTrue(retrievedDescriptor.isPresent());
        assertThat(retrievedDescriptor.get().getExternalId(), is(collectionDescriptor.getExternalId()));
        assertThat(retrievedDescriptor.get().getType(), is(CollectionDescriptor.Type.EMBEDDED));
        EmbeddedCoordinates embeddedCoordinates = retrievedDescriptor.get().getCoordinates().getEmbeddedCoordinates();
        assertThat(embeddedCoordinates.getHostId().getExternalId(), is(hostExternalId));
        assertThat(embeddedCoordinates.getHostFieldName(), is("items"));
    }

    private Descriptor createADescriptor() {
        ObjectId objectId = new ObjectId();
        return MongoDescriptorFactory.create(objectId, "test_model");
    }
}
