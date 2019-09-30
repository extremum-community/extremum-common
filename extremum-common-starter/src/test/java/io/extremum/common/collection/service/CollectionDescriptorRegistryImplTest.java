package io.extremum.common.collection.service;

import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CollectionDescriptorRegistryImplTest {
    @InjectMocks
    private CollectionDescriptorRegistryImpl registry;

    @Mock
    private CollectionDescriptorService collectionDescriptorService;

    private final CollectionDescriptor collectionDescriptor = CollectionDescriptor.forFree("the-name");
    private final Descriptor descriptorInDB = Descriptor.forCollection("external-id", collectionDescriptor);

    @Test
    void whenGettingFreeCollectionViaRegistry_thenItShouldBeRetrievedOrCreated() {
        when(collectionDescriptorService.retrieveByCoordinatesOrCreate(collectionDescriptor))
                .thenReturn(descriptorInDB);

        Descriptor descriptor = registry.freeCollection("the-name");

        assertThat(descriptor, is(sameInstance(descriptorInDB)));
    }
}