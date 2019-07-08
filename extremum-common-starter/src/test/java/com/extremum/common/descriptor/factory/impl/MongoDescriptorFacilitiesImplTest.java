package com.extremum.common.descriptor.factory.impl;

import com.extremum.common.descriptor.service.DescriptorService;
import com.extremum.sharedmodels.descriptor.Descriptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.extremum.common.descriptor.factory.impl.StringifiedObjectIdMatcher.objectId;
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
    @InjectMocks
    private MongoDescriptorFacilitiesImpl facilities;

    @Spy
    private DescriptorService descriptorService = new InMemoryDescriptorService();

    @Test
    void whenCreatingANewDescriptorWithANewInternalId_thenARandomObjectIdShouldBeGeneratedAndDescriptorSavedWithThatId() {
        when(descriptorService.createExternalId()).thenReturn("external-id");

        Descriptor descriptor = facilities.createWithNewInternalId("Test");

        assertThat(descriptor, is(notNullValue()));
        assertThat(descriptor.getExternalId(), is("external-id"));
        assertThat(descriptor.getInternalId(), is(objectId()));
        assertThat(descriptor.getModelType(), is("Test"));
        assertThat(descriptor.getStorageType(), is(Descriptor.StorageType.MONGO));

        verify(descriptorService).store(descriptor);
    }
}