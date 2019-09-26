package io.extremum.common.collection.service;

import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReactiveCollectionDescriptorRegistryImplTest {
    @InjectMocks
    private ReactiveCollectionDescriptorRegistryImpl registry;

    @Mock
    private ReactiveCollectionDescriptorService reactiveCollectionDescriptorService;

    private final CollectionDescriptor collectionDescriptor = CollectionDescriptor.forFree("the-name");
    private final Descriptor descriptorInDB = Descriptor.forCollection("external-id", collectionDescriptor);

    @Test
    void whenGettingFreeCollectionViaRegistry_thenItShouldBeRetrievedOrCreated() {
        when(reactiveCollectionDescriptorService.retrieveByCoordinatesOrCreate(collectionDescriptor))
                .thenReturn(Mono.just(descriptorInDB));

        Descriptor descriptor = registry.freeCollection("the-name").block();

        assertThat(descriptor, is(sameInstance(descriptorInDB)));
    }
}