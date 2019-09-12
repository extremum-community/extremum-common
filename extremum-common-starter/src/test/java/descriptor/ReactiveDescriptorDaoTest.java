package descriptor;

import config.DescriptorConfiguration;
import io.extremum.common.descriptor.dao.ReactiveDescriptorDao;
import io.extremum.common.descriptor.dao.impl.DescriptorRepository;
import io.extremum.common.descriptor.factory.DescriptorSaver;
import io.extremum.mongo.facilities.MongoDescriptorFacilities;
import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.common.test.TestWithServices;
import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;


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

    @Test
    void testStore() {
        Mono<Descriptor> mono = reactiveDescriptorDao.store(Descriptor.builder()
                .externalId(descriptorService.createExternalId())
                .internalId(new ObjectId().toString())
                .storageType(Descriptor.StorageType.MONGO)
                .build());
        Descriptor savedDescriptor = mono.block();

        Descriptor loadedDescriptor = descriptorRepository.findByExternalId(savedDescriptor.getExternalId())
                .orElse(null);
        assertThat(loadedDescriptor, is(notNullValue()));
        assertThat(loadedDescriptor.getExternalId(), is(savedDescriptor.getExternalId()));
        assertThat(loadedDescriptor.getInternalId(), is(savedDescriptor.getInternalId()));
        assertThat(loadedDescriptor.getStorageType(), is(Descriptor.StorageType.MONGO));
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
                .storageType(Descriptor.StorageType.MONGO)
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
}
