package descriptor;

import config.DescriptorConfiguration;
import io.extremum.common.descriptor.dao.ReactiveDescriptorDao;
import io.extremum.common.descriptor.dao.impl.DescriptorCodecs;
import io.extremum.common.descriptor.dao.impl.DescriptorRepository;
import io.extremum.common.descriptor.factory.DescriptorSaver;
import io.extremum.common.descriptor.factory.DescriptorSavers;
import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.common.redisson.CompositeCodecWithQuickFix;
import io.extremum.common.test.TestWithServices;
import io.extremum.mongo.facilities.MongoDescriptorFacilities;
import io.extremum.mongo.springdata.MainMongoDb;
import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.StandardStorageType;
import io.extremum.starter.properties.DescriptorsProperties;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;

import static java.util.Collections.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(classes = DescriptorConfiguration.class)
class ReactiveDescriptorDaoTest extends TestWithServices {
    @Autowired
    private ReactiveDescriptorDao reactiveDescriptorDao;
    @Autowired
    private DescriptorRepository descriptorRepository;

    @Autowired
    private DescriptorService descriptorService;
    @Autowired
    private MongoDescriptorFacilities mongoDescriptorFacilities;
    @Autowired
    private DescriptorSaver descriptorSaver;
    @Autowired
    @MainMongoDb
    private ReactiveTransactionManager reactiveTransactionManager;
    @Autowired
    private DescriptorsProperties descriptorsProperties;
    @Autowired
    private RedissonClient redissonClient;

    RMap<String, Descriptor> descriptorsInRedis;

    @BeforeEach
    void createRedisMap() {
        descriptorsInRedis = redissonClient.getMap(
                descriptorsProperties.getDescriptorsMapName(),
                new CompositeCodecWithQuickFix(new StringCodec(), DescriptorCodecs.codecForDescriptor())
        );
    }

    @Test
    void shouldStoreBothToMongoAndRedis() {
        Descriptor descriptor = descriptorForStorage();

        Mono<Descriptor> mono = reactiveDescriptorDao.store(descriptor);
        Descriptor savedDescriptor = mono.block();

        assertThatDescriptorWasSavedToMongoAndRedis(savedDescriptor);
    }

    private Descriptor descriptorForStorage() {
        return Descriptor.builder()
                .externalId(descriptorService.createExternalId())
                .internalId(new ObjectId().toString())
                .storageType(StandardStorageType.MONGO)
                .build();
    }

    private void assertThatDescriptorWasSavedToMongoAndRedis(Descriptor savedDescriptor) {
        assertThat(savedDescriptor, is(notNullValue()));

        Descriptor fromMongo = descriptorRepository.findByExternalId(savedDescriptor.getExternalId())
                .orElse(null);
        assertThat(fromMongo, is(notNullValue()));
        assertThat(fromMongo.getExternalId(), is(savedDescriptor.getExternalId()));
        assertThat(fromMongo.getInternalId(), is(savedDescriptor.getInternalId()));
        assertThat(fromMongo.getStorageType(), is("mongo"));

        Descriptor fromRedis = descriptorsInRedis.get(savedDescriptor.getExternalId());
        assertThat(fromRedis, is(notNullValue()));
        assertThat(fromRedis.getExternalId(), is(savedDescriptor.getExternalId()));
        assertThat(fromRedis.getInternalId(), is(savedDescriptor.getInternalId()));
        assertThat(fromRedis.getStorageType(), is("mongo"));
    }

    @Test
    void shouldStoreBothToMongoAndRedisUnderTransaction() {
        Descriptor descriptor = descriptorForStorage();

        TransactionalOperator txOp = TransactionalOperator.create(reactiveTransactionManager);

        Mono<Descriptor> mono = txOp.transactional(reactiveDescriptorDao.store(descriptor));
        Descriptor savedDescriptor = mono.block();

        assertThatDescriptorWasSavedToMongoAndRedis(savedDescriptor);
    }

    @Test
    void shouldStoreToRedisOnlyAfterTransactionCommits() {
        Descriptor descriptor = descriptorForStorage();

        TransactionalOperator txOp = TransactionalOperator.create(reactiveTransactionManager);

        Mono<Descriptor> storeAndAssertNoDescriptorSavedToRedis = reactiveDescriptorDao.store(descriptor)
                .doOnNext(savedDescriptor -> {
                    assertThatDescriptorIsNotSavedToRedis(savedDescriptor.getExternalId());
                });
        Mono<Descriptor> mono = txOp.transactional(storeAndAssertNoDescriptorSavedToRedis);
        mono.block();
    }

    private void assertThatDescriptorIsNotSavedToRedis(String externalId) {
        Descriptor fromRedis = descriptorsInRedis.get(externalId);
        assertThat(fromRedis, is(nullValue()));
    }

    @Test
    void testRetrieveByExternalId() {
        ObjectId objectId = new ObjectId();
        Descriptor originalDescriptor = mongoDescriptorFacilities.create(objectId, "test_model");

        String externalId = originalDescriptor.getExternalId();
        assertNotNull(externalId);

        Mono<Descriptor> mono = reactiveDescriptorDao.retrieveByExternalId(externalId);
        Descriptor foundDescriptor = mono.block();
        assertThat(foundDescriptor, is(notNullValue()));
        assertThat(foundDescriptor.getExternalId(), is(equalTo(originalDescriptor.getExternalId())));
    }

    @Test
    void testRetrieveByInternalId() {
        ObjectId objectId = new ObjectId();
        Descriptor originalDescriptor = mongoDescriptorFacilities.create(objectId, "test_model");

        String externalId = originalDescriptor.getExternalId();
        assertNotNull(externalId);

        Mono<Descriptor> mono = reactiveDescriptorDao.retrieveByInternalId(objectId.toString());
        Descriptor foundDescriptor = mono.block();
        assertThat(foundDescriptor, is(notNullValue()));
        assertThat(foundDescriptor.getExternalId(), is(equalTo(originalDescriptor.getExternalId())));
    }

    @Test
    void testRetrieveByCoordinatesString() {
        ObjectId objectId = new ObjectId();
        Descriptor hostDescriptor = mongoDescriptorFacilities.create(objectId, "test_model");

        String hostId = hostDescriptor.getExternalId();
        assertNotNull(hostId);

        Descriptor collectionDescriptor = descriptorSaver.createAndSave(
                CollectionDescriptor.forOwned(hostDescriptor, "items"));

        Mono<Descriptor> mono = reactiveDescriptorDao.retrieveByCollectionCoordinates(
                collectionDescriptor.getCollection().toCoordinatesString());
        Descriptor foundDescriptor = mono.block();
        assertThat(foundDescriptor, is(notNullValue()));
        assertThat(foundDescriptor.getExternalId(), is(equalTo(collectionDescriptor.getExternalId())));
    }

    @Test
    void testRetrieveFromMongo() {
        String internalId = new ObjectId().toString();
        Descriptor originalDescriptor = Descriptor.builder()
                .externalId(createExternalId())
                .internalId(internalId)
                .modelType("test_model")
                .storageType(StandardStorageType.MONGO)
                .build();

        Mono<Descriptor> mono = reactiveDescriptorDao.retrieveByInternalId(internalId);
        assertThat(mono.block(), is(nullValue()));

        descriptorRepository.save(originalDescriptor);
        mono = reactiveDescriptorDao.retrieveByInternalId(internalId);
        Descriptor foundDescriptor = mono.block();

        assertThat(foundDescriptor, is(notNullValue()));
        assertThat(foundDescriptor.getExternalId(), is(equalTo(originalDescriptor.getExternalId())));
    }

    @NotNull
    private String createExternalId() {
        return descriptorService.createExternalId();
    }

    @Test
    void givenADescriptorWithAnInternalIdAlreadyExists_whenSavingAnotherDescriptorWithTheSameInternalId_thenAnExceptionShouldBeThrown() {
        ObjectId objectId = new ObjectId();
        mongoDescriptorFacilities.create(objectId, "test_model");

        try {
            mongoDescriptorFacilities.create(objectId, "test_model");
            fail("An exception should be thrown");
        } catch (DuplicateKeyException e) {
            assertThat(e.getMessage(), containsString("duplicate key error"));
        }
    }

    @Test
    void givenADescriptorIsSaved_whenItIsRetrieved_thenItsCreatedModifiedAndVersionShouldBeFilled() {
        Descriptor descriptorToSave = new DescriptorSavers(descriptorService)
                .createSingleDescriptor(new ObjectId().toString(), StandardStorageType.MONGO);
        Descriptor savedDescriptor = reactiveDescriptorDao.store(descriptorToSave).block();
        assertThat(savedDescriptor, is(notNullValue()));

        Descriptor retrievedDescriptor = reactiveDescriptorDao.retrieveByExternalId(savedDescriptor.getExternalId())
                .block();
        assertThat(retrievedDescriptor, is(notNullValue()));

        assertThatAutoFieldsAreFilledCorrectly(retrievedDescriptor);
    }

    private void assertThatAutoFieldsAreFilledCorrectly(Descriptor retrievedDescriptor) {
        assertThat(retrievedDescriptor.getCreated(), is(notNullValue()));
        assertThat(retrievedDescriptor.getModified(), is(notNullValue()));
        assertThat(retrievedDescriptor.getVersion(), is(0L));
    }

    @Test
    void givenADescriptorExists_whenRetrievingItWithInternalIdsCollection_thenItsExternalIdShouldBeReturnedInAMap() {
        ObjectId objectId = new ObjectId();
        Descriptor descriptor = mongoDescriptorFacilities.create(objectId, "test_model");

        Mono<Map<String, String>> mono = reactiveDescriptorDao.retrieveMapByInternalIds(
                singletonList(objectId.toString()));

        StepVerifier.create(mono)
                .expectNext(singletonMap(descriptor.getInternalId(), descriptor.getExternalId()))
                .verifyComplete();
    }
}
