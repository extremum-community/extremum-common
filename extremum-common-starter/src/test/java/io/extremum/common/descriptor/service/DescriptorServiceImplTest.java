package io.extremum.common.descriptor.service;

import io.extremum.common.descriptor.dao.DescriptorDao;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.DescriptorNotReadyException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
class DescriptorServiceImplTest {
    @InjectMocks
    private DescriptorServiceImpl descriptorService;

    @Mock
    private DescriptorDao descriptorDao;

    @Test
    void whenNullDescriptorIsStored_thenNullPointerExceptionShouldBeThrown() {
        try {
            descriptorService.store(null);
            fail("An exception should be thrown");
        } catch (NullPointerException e) {
            assertThat(e.getMessage(), is("Descriptor is null"));
        }
    }

    @Test
    void whenLoadingByNullExternalId_thenNullPointerExceptionShouldBeThrown() {
        try {
            descriptorService.loadByExternalId(null);
            fail("An exception should be thrown");
        } catch (NullPointerException e) {
            assertThat(e.getMessage(), is("externalId is null"));
        }
    }

    @Test
    void whenLoadingByNullInternalId_thenNullPointerExceptionShouldBeThrown() {
        try {
            descriptorService.loadByInternalId(null);
            fail("An exception should be thrown");
        } catch (NullPointerException e) {
            assertThat(e.getMessage(), is("internalId is null"));
        }
    }

    @Test
    void givenDescriptorIsBlank_whenLoadingItByExternalId_thenDescriptorNotReadyExceptionShouldBeThrown() {
        when(descriptorDao.retrieveByExternalId("external-id"))
                .thenReturn(Optional.of(blankDescriptor()));

        assertThrows(DescriptorNotReadyException.class, () -> descriptorService.loadByExternalId("external-id"));
    }

    private Descriptor blankDescriptor() {
        return Descriptor.builder()
                .externalId("external-id")
                .storageType(Descriptor.StorageType.MONGO)
                .readiness(Descriptor.Readiness.BLANK)
                .build();
    }
}