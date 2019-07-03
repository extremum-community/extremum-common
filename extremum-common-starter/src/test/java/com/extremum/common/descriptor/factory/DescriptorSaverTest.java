package com.extremum.common.descriptor.factory;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.service.DescriptorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
class DescriptorSaverTest {
    @InjectMocks
    private DescriptorSaver descriptorSaver;
    @Mock
    private DescriptorService descriptorService;

    @Captor
    private ArgumentCaptor<Descriptor> descriptorCaptor;

    @Test
    void whenCreatingADescriptor_thenCorrectDataShouldBeSavedWithDescriptorService() {
        descriptorSaver.create("internal-id", "Test", Descriptor.StorageType.MONGO);

        verify(descriptorService).store(descriptorCaptor.capture());
        Descriptor savedDescriptor = descriptorCaptor.getValue();

        assertThatSavedDescriptorWithCorrectData(savedDescriptor);
    }

    private void assertThatSavedDescriptorWithCorrectData(Descriptor savedDescriptor) {
        assertThat(savedDescriptor.getExternalId(), is(notNullValue()));
        assertThat(savedDescriptor.getExternalId().length(), is(36));
        assertThat(savedDescriptor.getInternalId(), is("internal-id"));
        assertThat(savedDescriptor.getModelType(), is("Test"));
        assertThat(savedDescriptor.getStorageType(), is(Descriptor.StorageType.MONGO));
    }
}