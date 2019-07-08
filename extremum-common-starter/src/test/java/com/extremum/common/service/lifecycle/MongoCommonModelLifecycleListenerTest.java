package com.extremum.common.service.lifecycle;

import com.extremum.common.descriptor.factory.DescriptorFactory;
import com.extremum.common.descriptor.factory.DescriptorSaver;
import com.extremum.common.descriptor.factory.MongoDescriptorFacilities;
import com.extremum.common.descriptor.factory.impl.MongoDescriptorFacilitiesImpl;
import com.extremum.common.descriptor.service.DescriptorService;
import com.extremum.sharedmodels.descriptor.Descriptor;
import models.TestMongoModel;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
class MongoCommonModelLifecycleListenerTest {
    private MongoCommonModelLifecycleListener listener;

    @Mock
    private DescriptorService descriptorService;

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
    void givenAnEntityIsNew_whenItIsSaved_thenANewDescriptorShouldBeGeneratedAndAssignedToUuidAndItsInternalIdAssignedToId() {
        when(descriptorService.store(any())).thenReturn(descriptor);

        TestMongoModel modelToSave = new TestMongoModel();
        listener.onBeforeConvert(new BeforeConvertEvent<>(modelToSave, "does-not-matter"));

        assertThat(modelToSave.getUuid(), is(descriptor));
        assertThat(modelToSave.getId(), is(objectId));
    }
}