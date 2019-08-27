package descriptor;

import io.extremum.common.collection.dao.CollectionDescriptorDao;
import io.extremum.common.collection.dao.impl.CollectionDescriptorRepository;
import io.extremum.common.collection.service.CollectionDescriptorService;
import io.extremum.common.descriptor.factory.MongoDescriptorFacilities;
import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.common.test.TestWithServices;
import io.extremum.sharedmodels.descriptor.OwnedCoordinates;
import io.extremum.starter.CollectionDescriptorDaoFactory;
import io.extremum.starter.properties.DescriptorsProperties;
import io.extremum.starter.properties.RedisProperties;
import common.dao.mongo.MongoCommonDaoConfiguration;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest(classes = MongoCommonDaoConfiguration.class)
class CollectionDescriptorRedisSerializationTest extends TestWithServices {
    @Autowired
    private CollectionDescriptorService collectionDescriptorService;
    @Autowired
    private CollectionDescriptorRepository collectionDescriptorRepository;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private RedisProperties redisProperties;
    @Autowired
    private DescriptorsProperties descriptorsProperties;
    @Autowired
    private MongoDescriptorFacilities mongoDescriptorFacilities;

    private CollectionDescriptorDao freshDaoToAvoidCachingInMemory;

    private String hostExternalId;
    private CollectionDescriptor collectionDescriptor;

    @BeforeEach
    void init() {
        hostExternalId = createADescriptor();
        collectionDescriptor = createACollectionDescriptor(hostExternalId);

        freshDaoToAvoidCachingInMemory = CollectionDescriptorDaoFactory.create(redisProperties, descriptorsProperties,
                redissonClient, collectionDescriptorRepository);
    }

    @Test
    void whenLoadingACollectionDescriptorByExternalIdFromRedisWithoutMemoryCaching_thenDeserializationShouldSucceed() {
        Optional<CollectionDescriptor> retrievedDescriptor = freshDaoToAvoidCachingInMemory.retrieveByExternalId(
                collectionDescriptor.getExternalId());

        assertThatCollectionDescriptorRetrievalWasOk(hostExternalId, collectionDescriptor, retrievedDescriptor);
    }

    @Test
    void whenLoadingACollectionDescriptorByCoordinatesFromRedisWithoutMemoryCaching_thenDeserializationShouldSucceed() {
        Optional<CollectionDescriptor> retrievedDescriptor = freshDaoToAvoidCachingInMemory.retrieveByCoordinates(
                collectionDescriptor.getCoordinatesString());

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
        assertThat(ownedCoordinates.getHostAttributeName(), is("items"));
    }

    @NotNull
    private CollectionDescriptor createACollectionDescriptor(String hostExternalId) {
        throw new UnsupportedOperationException("Not implemented yet");
//        CollectionDescriptor collectionDescriptor = CollectionDescriptor.forOwned(
//                new Descriptor(hostExternalId), "items");
//
//        collectionDescriptorService.store(collectionDescriptor);
//
//        assertThat(collectionDescriptor.getExternalId(), is(notNullValue()));
//        return collectionDescriptor;
    }

    private String createADescriptor() {
        ObjectId objectId = new ObjectId();
        Descriptor hostDescriptor = mongoDescriptorFacilities.create(objectId, "test_model");
        return hostDescriptor.getExternalId();
    }
}
