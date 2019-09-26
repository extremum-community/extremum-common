package descriptor;

import io.extremum.common.descriptor.dao.DescriptorDao;
import io.extremum.common.descriptor.dao.impl.DescriptorRepository;
import io.extremum.common.descriptor.factory.DescriptorSaver;
import io.extremum.mongo.facilities.MongoDescriptorFacilities;
import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.common.test.TestWithServices;
import io.extremum.sharedmodels.basic.IntegerOrString;
import io.extremum.sharedmodels.basic.StringOrMultilingual;
import io.extremum.sharedmodels.content.Display;
import io.extremum.sharedmodels.content.Media;
import io.extremum.sharedmodels.content.MediaType;
import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.starter.DescriptorDaoFactory;
import io.extremum.starter.properties.DescriptorsProperties;
import io.extremum.starter.properties.RedisProperties;
import config.DescriptorConfiguration;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(classes = DescriptorConfiguration.class)
class DescriptorDaoTest extends TestWithServices {
    @Autowired
    private DescriptorDao descriptorDao;
    @Autowired
    private DescriptorRepository descriptorRepository;

    @Autowired
    private DescriptorService descriptorService;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private RedisProperties redisProperties;
    @Autowired
    private DescriptorsProperties descriptorsProperties;
    @Autowired
    private MongoDescriptorFacilities mongoDescriptorFacilities;
    @Autowired
    private DescriptorSaver descriptorSaver;

    private DescriptorDao freshDaoToAvoidCachingInMemory;

    @BeforeEach
    void init() {
        freshDaoToAvoidCachingInMemory = DescriptorDaoFactory.create(redisProperties, descriptorsProperties,
                redissonClient, descriptorRepository);
    }

    @Test
    void testRetrieveByExternalId() {
        Descriptor descriptor = saveADescriptor();

        String externalId = descriptor.getExternalId();
        assertNotNull(externalId);

        Optional<Descriptor> retrievedDescriptor = descriptorDao.retrieveByExternalId(externalId);
        assertTrue(retrievedDescriptor.isPresent());
        assertEquals(descriptor, retrievedDescriptor.get());
    }

    private Descriptor saveADescriptor() {
        ObjectId objectId = new ObjectId();
        return mongoDescriptorFacilities.create(objectId, "test_model");
    }

    @Test
    void testRetrieveByInternalId() {
        ObjectId objectId = new ObjectId();
        Descriptor descriptor = mongoDescriptorFacilities.create(objectId, "test_model");

        String externalId = descriptor.getExternalId();
        assertNotNull(externalId);

        Optional<Descriptor> retrievedDescriptor = descriptorDao.retrieveByInternalId(objectId.toString());
        assertTrue(retrievedDescriptor.isPresent());
        assertEquals(descriptor, retrievedDescriptor.get());
    }

    @Test
    void testRetrieveByCollectionCoordinates() {
        ObjectId hostId = new ObjectId();
        Descriptor hostDescriptor = mongoDescriptorFacilities.create(hostId, "test_model");

        Descriptor descriptor = Descriptor.forCollection("external-id",
                CollectionDescriptor.forOwned(hostDescriptor, "attr"));
        descriptorDao.store(descriptor);

        Descriptor retrievedDescriptor = descriptorDao.retrieveByCollectionCoordinates(
                descriptor.getCollection().toCoordinatesString()).orElse(null);
        assertThat(retrievedDescriptor, is(notNullValue()));
        assertThatRetrievedCollectionIsAsExpected(descriptor, retrievedDescriptor);
    }

    private void assertThatRetrievedCollectionIsAsExpected(Descriptor descriptor, Descriptor retrievedDescriptor) {
        assertEquals(descriptor.getExternalId(), retrievedDescriptor.getExternalId());
        assertThat(retrievedDescriptor.getType(), is(Descriptor.Type.COLLECTION));
        assertThat(retrievedDescriptor.getCollection(), is(notNullValue()));
        assertThat(retrievedDescriptor.getCollection().getType(), is(CollectionDescriptor.Type.OWNED));
        assertThat(retrievedDescriptor.getCollection().getCoordinatesString(),
                is(equalTo(descriptor.getCollection().toCoordinatesString())));
    }

    @Test
    void testRetrieveMapByExternalIds() {
        ObjectId objectId = new ObjectId();
        Descriptor descriptor = mongoDescriptorFacilities.create(objectId, "test_model");

        String externalId = descriptor.getExternalId();
        assertNotNull(externalId);

        Map<String, String> retrievedMap = descriptorDao.retrieveMapByExternalIds(Collections.singleton(externalId));
        assertEquals(1, retrievedMap.size());
        assertEquals(objectId.toString(), retrievedMap.get(externalId));
    }

    @Test
    void testRetrieveMapByInternalIds() {
        ObjectId objectId = new ObjectId();
        Descriptor descriptor = mongoDescriptorFacilities.create(objectId, "test_model");

        String externalId = descriptor.getExternalId();
        assertNotNull(externalId);

        Map<String, String> retrievedMap = descriptorDao.retrieveMapByInternalIds(Collections.singleton(objectId.toString()));
        assertEquals(1, retrievedMap.size());
        assertEquals(externalId, retrievedMap.get(objectId.toString()));
    }

    @Test
    void testRetrieveFromMongo() {
        String internalId = new ObjectId().toString();
        Descriptor descriptor = Descriptor.builder()
                .externalId(createExternalId())
                .internalId(internalId)
                .modelType("test_model")
                .storageType(Descriptor.StorageType.MONGO)
                .build();

        Optional<Descriptor> retrievedDescriptor = descriptorDao.retrieveByInternalId(internalId);
        assertFalse(retrievedDescriptor.isPresent());

        descriptorRepository.save(descriptor);
        retrievedDescriptor = descriptorDao.retrieveByInternalId(internalId);
        assertTrue(retrievedDescriptor.isPresent());
        assertEquals(descriptor, retrievedDescriptor.get());
    }

    @NotNull
    private String createExternalId() {
        return descriptorService.createExternalId();
    }

    @Test
    void testSaveDisplayFieldAsNull() {
        String internalId = new ObjectId().toString();
        String externalId = createExternalId();
        Descriptor descriptor = Descriptor.builder()
                .externalId(externalId)
                .internalId(internalId)
                .modelType("test_model")
                .storageType(Descriptor.StorageType.MONGO)
                .build();

        descriptorDao.store(descriptor);

        Optional<Descriptor> retrieved = freshDaoToAvoidCachingInMemory.retrieveByExternalId(externalId);
        assertTrue(retrieved.isPresent());
        assertNull(retrieved.get().getDisplay());
    }

    @Test
    void testSaveDisplayFieldAsString() {
        String internalId = new ObjectId().toString();
        String externalId = createExternalId();
        Descriptor descriptor = Descriptor.builder()
                .externalId(externalId)
                .internalId(internalId)
                .modelType("test_model")
                .storageType(Descriptor.StorageType.MONGO)
                .display(new Display("abcd"))
                .build();

        descriptorDao.store(descriptor);

        Optional<Descriptor> retrieved = freshDaoToAvoidCachingInMemory.retrieveByExternalId(externalId);
        assertTrue(retrieved.isPresent());
        assertTrue(retrieved.get().getDisplay().isString());
        assertEquals("abcd", retrieved.get().getDisplay().getStringValue());
    }

    @Test
    void testDisplayFieldDeserialization() {
        Media iconObj = new Media();
        iconObj.setUrl("/url/to/resource");
        iconObj.setType(MediaType.IMAGE);
        iconObj.setWidth(100);
        iconObj.setHeight(200);
        iconObj.setDepth(2);
        iconObj.setDuration(new IntegerOrString(20));

        Display displayObj = new Display(
                new StringOrMultilingual("aaa"),
                iconObj,
                iconObj
        );

        String internalId = new ObjectId().toString();
        String externalId = createExternalId();
        Descriptor descriptor = Descriptor.builder()
                .externalId(externalId)
                .internalId(internalId)
                .modelType("test_model")
                .storageType(Descriptor.StorageType.MONGO)
                .display(displayObj)
                .build();

        descriptorDao.store(descriptor);

        Optional<Descriptor> retrieved = freshDaoToAvoidCachingInMemory.retrieveByExternalId(externalId);
        assertTrue(retrieved.isPresent());

        Descriptor retrievedDescriptor = retrieved.get();
        Display display = retrievedDescriptor.getDisplay();

        assertNotNull(display);
        assertTrue(display.isObject());
        Media icon = display.getIcon();
        assertNotNull(icon);

        assertEquals("/url/to/resource", icon.getUrl());
        assertEquals(MediaType.IMAGE, icon.getType());
        assertEquals(100, (int) icon.getWidth());
        assertEquals(200, (int) icon.getHeight());
        assertEquals(2, (int) icon.getDepth());
        assertNotNull(icon.getDuration());
        assertTrue(icon.getDuration().isInteger());
        assertEquals(20, (int) icon.getDuration().getIntegerValue());

        Media splash = display.getSplash();
        assertNotNull(splash);

        assertEquals("/url/to/resource", splash.getUrl());
        assertEquals(MediaType.IMAGE, splash.getType());
        assertEquals(100, (int) splash.getWidth());
        assertEquals(200, (int) splash.getHeight());
        assertEquals(2, (int) splash.getDepth());
        assertNotNull(splash.getDuration());
        assertTrue(splash.getDuration().isInteger());
        assertEquals(20, (int) splash.getDuration().getIntegerValue());
    }

    @Test
    void givenADescriptorExists_whenItIsSearchedFor_thenItShouldBeFound() {
        Descriptor descriptor = saveADescriptor();

        Optional<Descriptor> optDescriptor = descriptorRepository.findByExternalId(descriptor.getExternalId());
        assertThat(optDescriptor.isPresent(), is(true));
    }

    @Test
    void givenADescriptorIsSoftDeleted_whenItIsSearchedFor_thenItShouldNotBeFound() {
        Descriptor descriptor = saveADescriptor();

        descriptor.setDeleted(true);
        descriptorRepository.save(descriptor);

        Optional<Descriptor> optDescriptor = descriptorRepository.findByExternalId(descriptor.getExternalId());
        assertThat(optDescriptor.isPresent(), is(false));
    }

    @Test
    void givenADescriptorWithAnInternalIdAlreadyExists_whenSavingAnotherDescriptorWithTheSameInternalId_thenAnExceptionShouldBeThrown() {
        Descriptor descriptor = saveADescriptor();

        try {
            mongoDescriptorFacilities.create(new ObjectId(descriptor.getInternalId()), "test_model");
            fail("An exception should be thrown");
        } catch (DuplicateKeyException  e) {
            assertThat(e.getMessage(), containsString("duplicate key error"));
        }
    }

    @Test
    void givenACollectionDescriptorAlreadyExists_whenSavingAnotherCollectionDescriptorWithTheSameCoordinates_thenAnExceptionShouldBeThrown() {
        Descriptor host = saveADescriptor();
        CollectionDescriptor collectionDescriptor = CollectionDescriptor.forOwned(host, "items");
        descriptorSaver.createAndSave(collectionDescriptor);

        try {
            descriptorSaver.createAndSave(collectionDescriptor);
            fail("An exception should be thrown");
        } catch (DuplicateKeyException  e) {
            assertThat(e.getMessage(), containsString("duplicate key error"));
        }
    }
}
