package descriptor;

import common.dao.mongo.MongoCommonDaoConfiguration;
import io.extremum.common.collection.CollectionDescriptor;
import io.extremum.common.collection.OwnedCoordinates;
import io.extremum.common.collection.dao.ReactiveCollectionDescriptorDao;
import io.extremum.common.collection.dao.impl.CollectionDescriptorRepository;
import io.extremum.common.collection.service.CollectionDescriptorService;
import io.extremum.common.descriptor.factory.MongoDescriptorFacilities;
import io.extremum.common.test.TestWithServices;
import io.extremum.sharedmodels.descriptor.Descriptor;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;


@SpringBootTest(classes = MongoCommonDaoConfiguration.class)
class ReactiveCollectionDescriptorDaoTest extends TestWithServices {
    @Autowired
    private ReactiveCollectionDescriptorDao collectionDescriptorDao;
    @Autowired
    private CollectionDescriptorService collectionDescriptorService;
    @Autowired
    private CollectionDescriptorRepository collectionDescriptorRepository;
    @Autowired
    private MongoDescriptorFacilities mongoDescriptorFacilities;

    @Test
    void testRetrieveByExternalId() {
        String hostExternalId = createADescriptor();
        CollectionDescriptor collectionDescriptor = createACollectionDescriptor(hostExternalId);

        Mono<CollectionDescriptor> retrievedDescriptor = collectionDescriptorDao.retrieveByExternalId(
                collectionDescriptor.getExternalId());

        assertThatCollectionDescriptorRetrievalWasOk(hostExternalId, collectionDescriptor, retrievedDescriptor);
    }

    private void assertThatCollectionDescriptorRetrievalWasOk(String hostExternalId,
                                                              CollectionDescriptor collectionDescriptor,
                                                              Mono<CollectionDescriptor> collectionDescriptorMono) {
        CollectionDescriptor retrievedDescriptor = collectionDescriptorMono.block();
        assertThat(retrievedDescriptor, is(notNullValue()));
        assertThat(retrievedDescriptor.getExternalId(), is(collectionDescriptor.getExternalId()));
        assertThat(retrievedDescriptor.getType(), is(CollectionDescriptor.Type.OWNED));
        OwnedCoordinates ownedCoordinates = retrievedDescriptor.getCoordinates().getOwnedCoordinates();
        assertThat(ownedCoordinates.getHostId().getExternalId(), is(hostExternalId));
        assertThat(ownedCoordinates.getHostAttributeName(), is("items"));
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
        Descriptor hostDescriptor = mongoDescriptorFacilities.create(objectId, "test_model");
        return hostDescriptor.getExternalId();
    }

    @Test
    void givenCollectionDescriptorDoesNotExist_whenRetrievingItFromMongo_thenNothingShouldBeReturned() {
        Mono<CollectionDescriptor> retrievedDescriptor = collectionDescriptorDao.retrieveByExternalId(
                "no-such-collection-descriptor");
        assertThat(retrievedDescriptor.block(), is(nullValue()));
    }

    @Test
    void givenCollectionDescriptorExists_whenRetrievingItFromMongo_thenItShouldBeReturned() {
        String hostExternalId = createADescriptor();
        CollectionDescriptor descriptor = CollectionDescriptor.forOwned(new Descriptor(hostExternalId), "items");
        collectionDescriptorRepository.save(descriptor);

        Mono<CollectionDescriptor> mono = collectionDescriptorDao.retrieveByExternalId(descriptor.getExternalId());
        CollectionDescriptor retrievedCollectionDescriptor = mono.block();
        assertThat(retrievedCollectionDescriptor, is(notNullValue()));
        assertThat(retrievedCollectionDescriptor.getExternalId(), is(equalTo(descriptor.getExternalId())));
    }
}
