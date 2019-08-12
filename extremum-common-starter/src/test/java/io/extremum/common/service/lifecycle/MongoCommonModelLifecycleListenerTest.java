package io.extremum.common.service.lifecycle;

import io.extremum.common.descriptor.factory.DescriptorFactory;
import io.extremum.common.descriptor.factory.DescriptorSaver;
import io.extremum.common.descriptor.factory.MongoDescriptorFacilities;
import io.extremum.common.descriptor.factory.impl.InMemoryDescriptorService;
import io.extremum.common.descriptor.factory.impl.MongoDescriptorFacilitiesImpl;
import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.sharedmodels.descriptor.Descriptor;
import models.TestMongoModel;
import org.bson.types.ObjectId;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
class MongoCommonModelLifecycleListenerTest {
    private MongoCommonModelLifecycleListener listener;

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
        MongoDescriptorFacilities facilities = new MongoDescriptorFacilitiesImpl(new DescriptorFactory(),
                new DescriptorSaver(descriptorService));
        listener = new MongoCommonModelLifecycleListener(facilities);
    }

    @Test
    void givenAnEntityHasNeitherIdNorUUID_whenItIsSaved_thenANewDescriptorShouldBeGeneratedWithNewObjectIdAndAssignedToUuidAndItsInternalIdAssignedToId() {
        alwaysGenerateExpectedExternalId();
        TestMongoModel model = new TestMongoModel();
        
        listener.onBeforeConvert(new BeforeConvertEvent<>(model, "does-not-matter"));

        assertThatDescriptorWasGeneratedWithNewInternalId(model);
        assertThatDescriptorInternalIdMatchesEntityId(model);
        assertThatDescriptorWasSaved(model);
    }

    private void alwaysGenerateExpectedExternalId() {
        when(descriptorService.createExternalId()).thenReturn("new-external-id");
    }

    private void assertThatDescriptorWasGeneratedWithNewInternalId(TestMongoModel model) {
        MatcherAssert.assertThat(model.getUuid(), is(notNullValue()));
        MatcherAssert.assertThat(model.getUuid().getExternalId(), is("new-external-id"));
        MatcherAssert.assertThat(model.getUuid().getInternalId(), is(not(objectId.toString())));
        MatcherAssert.assertThat(model.getId(), is(notNullValue()));
    }

    private void assertThatDescriptorInternalIdMatchesEntityId(TestMongoModel model) {
        MatcherAssert.assertThat(model.getId().toString(), CoreMatchers.is(equalTo(model.getUuid().getInternalId())));
    }

    private void assertThatDescriptorWasSaved(TestMongoModel model) {
        verify(descriptorService).store(model.getUuid());
    }

    @Test
    void givenAnEntityHasNoIdButHasUUID_whenItIsSaved_thenDescriptorShouldNotBeGeneratedButUUIDsInternalIdAssignedToId() {
        TestMongoModel model = new TestMongoModel();
        model.setUuid(descriptor);

        listener.onBeforeConvert(new BeforeConvertEvent<>(model, "does-not-matter"));

        assertThatUUIDWasNotChanged(model);
        assertThatEntityIdWasTakenFromUUID(model);
        assertThatNoDescriptorWasSaved();
    }

    private void assertThatUUIDWasNotChanged(TestMongoModel model) {
        MatcherAssert.assertThat(model.getUuid(), is(sameInstance(descriptor)));
    }

    private void assertThatEntityIdWasTakenFromUUID(TestMongoModel model) {
        MatcherAssert.assertThat(model.getId(), is(objectId));
    }

    private void assertThatNoDescriptorWasSaved() {
        verify(descriptorService, never()).store(any());
    }

    @Test
    void givenAnEntityHasIdButNoUUID_whenItIsSaved_thenANewDescriptorShouldBeGeneratedForThatIdAndAssignedToUuid() {
        alwaysGenerateExpectedExternalId();
        TestMongoModel model = new TestMongoModel();
        model.setId(objectId);

        listener.onBeforeConvert(new BeforeConvertEvent<>(model, "does-not-matter"));

        assertThatDescriptorWasGeneratedWithGivenInternalId(model);
        assertThatEntityIdDidNotChange(model);
        assertThatDescriptorWasSaved(model);
    }

    private void assertThatDescriptorWasGeneratedWithGivenInternalId(TestMongoModel model) {
        MatcherAssert.assertThat(model.getUuid(), is(notNullValue()));
        MatcherAssert.assertThat(model.getUuid().getExternalId(), is("new-external-id"));
        MatcherAssert.assertThat(model.getUuid().getInternalId(), is(objectId.toString()));
    }

    private void assertThatEntityIdDidNotChange(TestMongoModel model) {
        MatcherAssert.assertThat(model.getId(), is(objectId));
    }

    @Test
    void givenAnEntityHasBothIdAndUUID_whenItIsSaved_thenNothingShouldHappen() {
        TestMongoModel model = new TestMongoModel();
        model.setId(objectId);
        model.setUuid(descriptor);

        listener.onBeforeConvert(new BeforeConvertEvent<>(model, "does-not-matter"));

        assertThatUUIDWasNotChanged(model);
        assertThatEntityIdDidNotChange(model);
        assertThatNoDescriptorWasSaved();
    }
}