package com.extremum.common.descriptor.factory.impl;

import com.extremum.common.descriptor.factory.DescriptorFactory;
import com.extremum.common.descriptor.factory.DescriptorSaver;
import com.extremum.common.descriptor.service.DescriptorService;
import com.extremum.sharedmodels.descriptor.Descriptor;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
class MongoDescriptorFacilitiesImplTest {
    private MongoDescriptorFacilitiesImpl facilities;

    @Spy
    private DescriptorService descriptorService = new InMemoryDescriptorService();

    private final ObjectId objectId = new ObjectId();

    @BeforeEach
    void initDescriptorSaver() {
        DescriptorSaver descriptorSaver = new DescriptorSaver(descriptorService);
        facilities = new MongoDescriptorFacilitiesImpl(new DescriptorFactory(), descriptorSaver);
    }

    @Test
    void whenCreatingANewDescriptorWithANewInternalId_thenARandomObjectIdShouldBeGeneratedAndDescriptorSavedWithThatId() {
        when(descriptorService.createExternalId()).thenReturn("external-id");

        Descriptor descriptor = facilities.create(objectId, "Test");

        assertThat(descriptor, is(notNullValue()));
        assertThat(descriptor.getExternalId(), is("external-id"));
        assertThat(descriptor.getInternalId(), is(equalTo(objectId.toString())));
        assertThat(descriptor.getModelType(), is("Test"));
        assertThat(descriptor.getStorageType(), is(Descriptor.StorageType.MONGO));

        verify(descriptorService).store(descriptor);
    }
}