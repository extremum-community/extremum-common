package io.extremum.common.descriptor.factory.impl;

import io.extremum.common.descriptor.factory.DescriptorFactory;
import io.extremum.common.descriptor.factory.ReactiveDescriptorSaver;
import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.common.descriptor.service.ReactiveDescriptorService;
import io.extremum.mongo.facilities.ReactiveMongoDescriptorFacilitiesImpl;
import io.extremum.sharedmodels.descriptor.Descriptor;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
class ReactiveMongoDescriptorFacilitiesImplTest {
    private ReactiveMongoDescriptorFacilitiesImpl facilities;

    @Spy
    private DescriptorService descriptorService = new InMemoryDescriptorService();
    @Spy
    private ReactiveDescriptorService reactiveDescriptorService = new InMemoryReactiveDescriptorService();

    private final ObjectId objectId = new ObjectId();

    @BeforeEach
    void initDescriptorSaver() {
        ReactiveDescriptorSaver descriptorSaver = new ReactiveDescriptorSaver(
                descriptorService, reactiveDescriptorService);
        facilities = new ReactiveMongoDescriptorFacilitiesImpl(new DescriptorFactory(), descriptorSaver);
    }

    @Test
    void whenCreatingANewDescriptorWithANewInternalId_thenARandomObjectIdShouldBeGeneratedAndDescriptorSavedWithThatId() {
        when(descriptorService.createExternalId()).thenReturn("external-id");

        Descriptor descriptor = facilities.create(objectId, "Test").block();

        assertThat(descriptor, is(notNullValue()));
        assertThat(descriptor.getExternalId(), is("external-id"));
        assertThat(descriptor.getInternalId(), is(equalTo(objectId.toString())));
        assertThat(descriptor.getModelType(), is("Test"));
        assertThat(descriptor.getStorageType(), is(Descriptor.StorageType.MONGO));

        //noinspection UnassignedFluxMonoInstance
        verify(reactiveDescriptorService).store(descriptor);
    }

    @Test
    void whenCreatingADescriptorFromInternalId_thenItShouldBeFilledWithInternalId() {
        Descriptor descriptor = facilities.fromInternalId(objectId).block();

        assertThat(descriptor.getInternalId(), is(equalTo(objectId.toString())));
        assertThat(descriptor.getStorageType(), is(Descriptor.StorageType.MONGO));
    }

    @Test
    void givenDescriptorIsForMongo_whenResolvingADescriptor_thenInternalIdShouldBeReturned() {
        Descriptor descriptor = Descriptor.builder()
                .internalId(objectId.toString())
                .storageType(Descriptor.StorageType.MONGO)
                .build();

        ObjectId resolvedId = facilities.resolve(descriptor).block();

        assertThat(resolvedId, is(equalTo(objectId)));
    }

    @Test
    void givenDescriptorIsNotForMongo_whenResolvingADescriptor_thenInternalIdShouldBeReturned() {
        Descriptor descriptor = Descriptor.builder()
                .internalId(objectId.toString())
                .storageType(Descriptor.StorageType.POSTGRES)
                .build();

        Mono<ObjectId> mono = facilities.resolve(descriptor);
        try {
            mono.block();
            fail("An exception should be thrown");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is("Wrong descriptor storage type POSTGRES"));
        }
    }
}