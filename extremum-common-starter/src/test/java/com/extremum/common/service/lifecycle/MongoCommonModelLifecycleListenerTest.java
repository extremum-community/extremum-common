package com.extremum.common.service.lifecycle;

import com.extremum.common.descriptor.factory.DescriptorFactory;
import com.extremum.common.descriptor.factory.DescriptorSaver;
import com.extremum.common.descriptor.factory.MongoDescriptorFacilities;
import com.extremum.common.descriptor.factory.impl.InMemoryDescriptorService;
import com.extremum.common.descriptor.factory.impl.MongoDescriptorFacilitiesImpl;
import com.extremum.common.descriptor.service.DescriptorService;
import com.extremum.sharedmodels.descriptor.Descriptor;
import models.TestMongoModel;
import org.bson.types.ObjectId;
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
            .externalId("external-id")
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
        when(descriptorService.createExternalId()).thenReturn("external-id");
        TestMongoModel model = new TestMongoModel();
        
        listener.onBeforeConvert(new BeforeConvertEvent<>(model, "does-not-matter"));

        assertThat(model.getUuid().getExternalId(), is("external-id"));
        assertThat(model.getUuid().getInternalId(), is(not(objectId.toString())));
        assertThat(model.getId(), is(notNullValue()));
        assertThat(model.getId().toString(), is(equalTo(model.getUuid().getInternalId())));

        verify(descriptorService).store(model.getUuid());
    }

    @Test
    void givenAnEntityHasNoIdButHasUUID_whenItIsSaved_thenDescriptorShouldNotBeGeneratedButUUIDsInternalIdAssignedToId() {
        TestMongoModel model = new TestMongoModel();
        model.setUuid(descriptor);

        listener.onBeforeConvert(new BeforeConvertEvent<>(model, "does-not-matter"));

        assertThat(model.getUuid(), is(sameInstance(descriptor)));
        assertThat(model.getId(), is(objectId));

        verify(descriptorService, never()).store(any());
    }

    @Test
    void givenAnEntityHasIdButNoUUID_whenItIsSaved_thenANewDescriptorShouldBeGeneratedForThatIdAndAssignedToUuid() {
        when(descriptorService.createExternalId()).thenReturn("external-id");
        TestMongoModel model = new TestMongoModel();
        model.setId(objectId);

        listener.onBeforeConvert(new BeforeConvertEvent<>(model, "does-not-matter"));

        assertThat(model.getUuid().getExternalId(), is("external-id"));
        assertThat(model.getUuid().getInternalId(), is(objectId.toString()));
        assertThat(model.getId(), is(objectId));

        verify(descriptorService).store(model.getUuid());
    }

    @Test
    void givenAnEntityHasBothIdAndUUID_whenItIsSaved_thenNothingShouldHappen() {
        TestMongoModel model = new TestMongoModel();
        model.setId(objectId);
        model.setUuid(descriptor);

        listener.onBeforeConvert(new BeforeConvertEvent<>(model, "does-not-matter"));

        assertThat(model.getUuid(), is(sameInstance(descriptor)));
        assertThat(model.getId(), is(objectId));

        verify(descriptorService, never()).store(any());
    }
}