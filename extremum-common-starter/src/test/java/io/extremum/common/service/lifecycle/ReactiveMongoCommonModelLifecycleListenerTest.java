package io.extremum.common.service.lifecycle;

import io.extremum.common.descriptor.factory.impl.InMemoryDescriptorService;
import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.mongo.facilities.ReactiveMongoDescriptorFacilities;
import io.extremum.mongo.service.lifecycle.ReactiveMongoCommonModelLifecycleListener;
import io.extremum.mongo.springdata.ReactiveBeforeConvertEvent;
import io.extremum.sharedmodels.descriptor.Descriptor;
import models.TestMongoModel;
import org.bson.types.ObjectId;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
class ReactiveMongoCommonModelLifecycleListenerTest {
    private ReactiveMongoCommonModelLifecycleListener listener;

    @Mock
    private ReactiveMongoDescriptorFacilities reactiveMongoDescriptorFacilities;

    @Spy
    private DescriptorService descriptorService = new InMemoryDescriptorService();

    private final ObjectId objectId = new ObjectId();
    private final Descriptor descriptor = Descriptor.builder()
            .externalId("existing-external-id")
            .internalId(objectId.toString())
            .modelType("Test")
            .storageType(Descriptor.StorageType.MONGO)
            .build();

    @BeforeEach
    void createListener() {
        listener = new ReactiveMongoCommonModelLifecycleListener(reactiveMongoDescriptorFacilities);
    }

    @Test
    void givenAnEntityHasNeitherIdNorUUID_whenItIsSaved_thenANewDescriptorShouldBeGeneratedWithNewObjectIdAndAssignedToUuidAndItsInternalIdAssignedToId() {
        createADescriptorWhenRequested();
        when(reactiveMongoDescriptorFacilities.resolve(any()))
                .then(invocation -> {
                    Descriptor descriptor = invocation.getArgument(0);
                    return Mono.just(new ObjectId(descriptor.getInternalId()));
                });

        TestMongoModel model = new TestMongoModel();
        
        listener.onBeforeConvert(new ReactiveBeforeConvertEvent<>(model)).block();

        assertThatDescriptorWasGeneratedWithNewInternalId(model);
        assertThatDescriptorInternalIdMatchesEntityId(model);
    }

    private void createADescriptorWhenRequested() {
        when(reactiveMongoDescriptorFacilities.create(any(), anyString()))
                .then(invocation -> Mono.just(
                        Descriptor.builder()
                                .externalId("new-external-id")
                                .internalId(invocation.getArgument(0).toString())
                                .modelType(invocation.getArgument(1))
                                .storageType(Descriptor.StorageType.MONGO)
                                .build()
                ));
    }

    private void assertThatDescriptorWasGeneratedWithNewInternalId(TestMongoModel model) {
        assertThat(model.getUuid(), is(notNullValue()));
        assertThat(model.getUuid().getExternalId(), is("new-external-id"));
        assertThat(model.getUuid().getInternalId(), is(not(objectId.toString())));
        assertThat(model.getId(), is(notNullValue()));
    }

    private void assertThatDescriptorInternalIdMatchesEntityId(TestMongoModel model) {
        assertThat(model.getId().toString(), CoreMatchers.is(equalTo(model.getUuid().getInternalId())));
    }

    @Test
    void givenAnEntityHasNoIdButHasUUID_whenItIsSaved_thenDescriptorShouldNotBeGeneratedButUUIDsInternalIdAssignedToId() {
        when(reactiveMongoDescriptorFacilities.resolve(descriptor))
                .thenReturn(Mono.just(objectId));

        TestMongoModel model = new TestMongoModel();
        model.setUuid(descriptor);

        listener.onBeforeConvert(new ReactiveBeforeConvertEvent<>(model)).block();

        assertThatUUIDWasNotChanged(model);
        assertThatEntityIdWasTakenFromUUID(model);
        assertThatNoDescriptorWasSaved();
    }

    private void assertThatUUIDWasNotChanged(TestMongoModel model) {
        assertThat(model.getUuid(), is(sameInstance(descriptor)));
    }

    private void assertThatEntityIdWasTakenFromUUID(TestMongoModel model) {
        assertThat(model.getId(), is(objectId));
    }

    private void assertThatNoDescriptorWasSaved() {
        verify(descriptorService, never()).store(any());
    }

    @Test
    void givenAnEntityHasIdButNoUUID_whenItIsSaved_thenANewDescriptorShouldBeGeneratedForThatIdAndAssignedToUuid() {
        createADescriptorWhenRequested();
        TestMongoModel model = new TestMongoModel();
        model.setId(objectId);

        listener.onBeforeConvert(new ReactiveBeforeConvertEvent<>(model)).block();

        assertThatDescriptorWasGeneratedWithGivenInternalId(model);
        assertThatEntityIdDidNotChange(model);
    }

    private void assertThatDescriptorWasGeneratedWithGivenInternalId(TestMongoModel model) {
        assertThat(model.getUuid(), is(notNullValue()));
        assertThat(model.getUuid().getExternalId(), is("new-external-id"));
        assertThat(model.getUuid().getInternalId(), is(objectId.toString()));
    }

    private void assertThatEntityIdDidNotChange(TestMongoModel model) {
        assertThat(model.getId(), is(objectId));
    }

    @Test
    void givenAnEntityHasBothIdAndUUID_whenItIsSaved_thenNothingShouldHappen() {
        TestMongoModel model = new TestMongoModel();
        model.setId(objectId);
        model.setUuid(descriptor);

        listener.onBeforeConvert(new ReactiveBeforeConvertEvent<>(model)).block();

        assertThatUUIDWasNotChanged(model);
        assertThatEntityIdDidNotChange(model);
        assertThatNoDescriptorWasSaved();
    }
}