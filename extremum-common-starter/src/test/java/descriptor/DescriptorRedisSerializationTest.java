package descriptor;

import config.DescriptorConfiguration;
import io.extremum.common.descriptor.dao.DescriptorDao;
import io.extremum.common.descriptor.dao.impl.DescriptorRepository;
import io.extremum.common.descriptor.factory.MongoDescriptorFacilities;
import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.common.test.TestWithServices;
import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.OwnedCoordinates;
import io.extremum.starter.DescriptorDaoFactory;
import io.extremum.starter.properties.DescriptorsProperties;
import io.extremum.starter.properties.RedisProperties;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest(classes = DescriptorConfiguration.class)
class DescriptorRedisSerializationTest extends TestWithServices {
    @Autowired
    private DescriptorService descriptorService;
    @Autowired
    private DescriptorRepository descriptorRepository;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private RedisProperties redisProperties;
    @Autowired
    private DescriptorsProperties descriptorsProperties;
    @Autowired
    private MongoDescriptorFacilities mongoDescriptorFacilities;

    private DescriptorDao freshDaoToAvoidCachingInMemory;

    private Descriptor descriptor;

    @BeforeEach
    void init() {
        descriptor = createADescriptor();
        
        freshDaoToAvoidCachingInMemory = DescriptorDaoFactory.create(redisProperties, descriptorsProperties,
                redissonClient, descriptorRepository);
    }

    @Test
    void whenLoadingADescriptorByExternalIdFromRedisWithoutMemoryCaching_thenDeserializationShouldSucceed() {
        Optional<Descriptor> retrievedDescriptor = freshDaoToAvoidCachingInMemory.retrieveByExternalId(
                descriptor.getExternalId());
        
        assertThatRetrievedDescriptorIsOk(descriptor, retrievedDescriptor);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private void assertThatRetrievedDescriptorIsOk(Descriptor descriptor, Optional<Descriptor> retrievedDescriptor) {
        assertTrue(retrievedDescriptor.isPresent());
        assertEquals(descriptor, retrievedDescriptor.get());
    }

    @Test
    void whenLoadingADescriptorByInternalIdFromRedisWithoutMemoryCaching_thenDeserializationShouldSucceed() {
        Optional<Descriptor> retrievedDescriptor = freshDaoToAvoidCachingInMemory.retrieveByInternalId(
                descriptor.getInternalId());

        assertThatRetrievedDescriptorIsOk(descriptor, retrievedDescriptor);
    }

    private Descriptor createADescriptor() {
        ObjectId objectId = new ObjectId();
        return mongoDescriptorFacilities.create(objectId, "test_model");
    }

    @Test
    void whenLoadingACollectionDescriptorByCoordinatesFromRedisWithoutMemoryCaching_thenDeserializationShouldSucceed() {
        Descriptor hostDescriptor = createADescriptor();
        CollectionDescriptor collectionDescriptor = CollectionDescriptor.forOwned(hostDescriptor, "items");
        Descriptor descriptor = Descriptor.forCollection(collectionDescriptor);
        descriptor.setExternalId(descriptorService.createExternalId());
        descriptorService.store(descriptor);

        Descriptor retrievedDescriptor = freshDaoToAvoidCachingInMemory.retrieveByCollectionCoordinates(
                collectionDescriptor.getCoordinatesString()).orElse(null);

        assertThat(retrievedDescriptor, is(notNullValue()));
        assertThatCollectionDescriptorRetrievalWasOk(hostDescriptor.getExternalId(),
                retrievedDescriptor);
    }

    private void assertThatCollectionDescriptorRetrievalWasOk(String hostExternalId,
                                                              Descriptor retrievedDescriptor) {
        assertThat(retrievedDescriptor.getExternalId(), is(notNullValue()));
        assertThat(retrievedDescriptor.getCollection().getType(), is(CollectionDescriptor.Type.OWNED));
        OwnedCoordinates ownedCoordinates = retrievedDescriptor.getCollection().getCoordinates().getOwnedCoordinates();
        assertThat(ownedCoordinates.getHostId().getExternalId(), is(hostExternalId));
        assertThat(ownedCoordinates.getHostAttributeName(), is("items"));
    }
}
