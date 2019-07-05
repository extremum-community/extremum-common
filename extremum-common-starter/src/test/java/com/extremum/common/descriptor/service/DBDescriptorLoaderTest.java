package com.extremum.common.descriptor.service;

import com.extremum.common.descriptor.Descriptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
class DBDescriptorLoaderTest {
    @InjectMocks
    private DBDescriptorLoader loader;

    @Mock
    private DescriptorService descriptorService;

    private Descriptor descriptor = new Descriptor("external-id");

    @Test
    void whenLoadingByExternalId_thenDescriptorShouldBeLoadedViaServiceByExternalId() {
        when(descriptorService.loadByExternalId("external-id"))
                .thenReturn(java.util.Optional.ofNullable(descriptor));

        Optional<Descriptor> result = loader.loadByExternalId("external-id");

        assertThat(result.orElse(null), is(sameInstance(descriptor)));
    }

    @Test
    void whenLoadingByInternalId_thenDescriptorShouldBeLoadedViaServiceByInternalId() {
        when(descriptorService.loadByInternalId("internal-id"))
                .thenReturn(java.util.Optional.ofNullable(descriptor));

        Optional<Descriptor> result = loader.loadByInternalId("internal-id");

        assertThat(result.orElse(null), is(sameInstance(descriptor)));
    }
}