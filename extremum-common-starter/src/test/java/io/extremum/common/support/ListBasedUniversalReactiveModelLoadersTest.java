package io.extremum.common.support;

import io.extremum.sharedmodels.descriptor.Descriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListBasedUniversalReactiveModelLoadersTest {
    private ListBasedUniversalReactiveModelLoaders loaders;

    @Mock
    private UniversalReactiveModelLoader loader;

    private final Descriptor mongoDescriptor = Descriptor.builder()
            .storageType(Descriptor.StorageType.MONGO)
            .build();

    @BeforeEach
    void createLoaders() {
        loaders = new ListBasedUniversalReactiveModelLoaders(Collections.singletonList(loader));
    }

    @Test
    void whenLoaderSupportsTheDescriptorStorageType_thenTheLoaderShouldBeReturned() {
        when(loader.type()).thenReturn(Descriptor.StorageType.MONGO);

        UniversalReactiveModelLoader foundLoader = loaders.findLoader(mongoDescriptor);

        assertThat(foundLoader, is(sameInstance(loader)));
    }

    @Test
    void whenLoaderDoesNotSupportTheDescriptorStorageType_thenAnExceptionShouldBeThrown() {
        when(loader.type()).thenReturn(Descriptor.StorageType.POSTGRES);

        try {
            loaders.findLoader(mongoDescriptor);
            fail("An exception should be thrown");
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), is("No loader supports storage type 'MONGO'"));
        }
    }
}