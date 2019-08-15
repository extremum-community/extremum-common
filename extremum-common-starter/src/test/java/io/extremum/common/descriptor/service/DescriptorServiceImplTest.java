package io.extremum.common.descriptor.service;

import io.extremum.common.descriptor.dao.DescriptorDao;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

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
}