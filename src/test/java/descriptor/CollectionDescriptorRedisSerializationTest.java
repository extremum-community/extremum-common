package descriptor;

import com.extremum.common.collection.CollectionDescriptor;
import com.extremum.common.collection.OwnedCoordinates;
import com.extremum.common.collection.dao.CollectionDescriptorDao;
import com.extremum.common.collection.dao.impl.CollectionDescriptorRepository;
import com.extremum.common.collection.service.CollectionDescriptorService;
import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.factory.impl.MongoDescriptorFactory;
import com.extremum.common.test.TestWithServices;
import com.extremum.starter.CollectionDescriptorDaoFactory;
import com.extremum.starter.properties.DescriptorsProperties;
import com.extremum.starter.properties.RedisProperties;
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

    private CollectionDescriptorDao freshDaoToAvoidCachingInMemory;

    @BeforeEach
    void init() {
        freshDaoToAvoidCachingInMemory = CollectionDescriptorDaoFactory.create(redisProperties, descriptorsProperties,
                redissonClient, collectionDescriptorRepository);
    }

    @Test
    void whenLoadingACollectionDescriptorByExternalIdFromRedisWithoutMemoryCaching_thenDeserializationShouldSucceed() {
        String hostExternalId = createADescriptor();
        CollectionDescriptor collectionDescriptor = createACollectionDescriptor(hostExternalId);

        Optional<CollectionDescriptor> retrievedDescriptor = freshDaoToAvoidCachingInMemory.retrieveByExternalId(
                collectionDescriptor.getExternalId());

        assertThatCollectionDescriptorRetrievalWasOk(hostExternalId, collectionDescriptor, retrievedDescriptor);
    }

    @Test
    void whenLoadingACollectionDescriptorByCoordinatesFromRedisWithoutMemoryCaching_thenDeserializationShouldSucceed() {
        String hostExternalId = createADescriptor();
        CollectionDescriptor collectionDescriptor = createACollectionDescriptor(hostExternalId);

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
}
