package io.extremum.common.collection.service;

import io.extremum.common.descriptor.dao.ReactiveDescriptorDao;
import io.extremum.common.descriptor.factory.impl.InMemoryDescriptorService;
import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import reactor.core.publisher.Mono;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReactiveCollectionDescriptorServiceImplTest {
    @InjectMocks
    private ReactiveCollectionDescriptorServiceImpl service;

    @Mock
    private ReactiveDescriptorDao reactiveDescriptorDao;
    @Spy
    private DescriptorService descriptorService = new InMemoryDescriptorService();

    @Captor
    private ArgumentCaptor<Descriptor> descriptorCaptor;

    private final CollectionDescriptor owned = CollectionDescriptor.forOwned(new Descriptor("host-id"), "items");
    private final Descriptor collDescriptorInDb = Descriptor.forCollection("externalId", owned);

    @Test
    void whenRetrievingACollectionByExternalId_thenItShouldBeRetrievedFromDao() {
        when(reactiveDescriptorDao.retrieveByExternalId("externalId"))
                .thenReturn(Mono.just(collDescriptorInDb));

        Mono<CollectionDescriptor> mono = service.retrieveByExternalId("externalId");

        assertThat(mono.block(), is(sameInstance(collDescriptorInDb.getCollection())));
    }

    @Test
    void givenDescriptorTypeIsNotCollection_whenRetrievingACollectionByExternalId_thenItShouldBeRetrievedFromDao() {
        collDescriptorInDb.setType(Descriptor.Type.SINGLE);
        when(reactiveDescriptorDao.retrieveByExternalId("externalId"))
                .thenReturn(Mono.just(collDescriptorInDb));

        try {
            service.retrieveByExternalId("externalId").block();
            fail("An exception should be thrown");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is("Descriptor 'externalId' must have type COLLECTION, but it is 'SINGLE'"));
        }
    }

    @Test
    void givenDescriptorHasNoCollection_whenRetrievingACollection_thenItShouldBeRetrievedFromDao() {
        collDescriptorInDb.setCollection(null);
        when(reactiveDescriptorDao.retrieveByExternalId("externalId"))
                .thenReturn(Mono.just(collDescriptorInDb));

        try {
            service.retrieveByExternalId("externalId").block();
            fail("An exception should be thrown");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(),
                    is("Descriptor 'externalId' has type COLLECTION, but there is no collection in it"));
        }
    }

    @Test
    void givenNoCollectionDescriptorExistsWithSuchCoordinates_whenRetrievingByCoordinatesOrCreating_thenDescriptorShouldBeSavedViaDao() {
        when(reactiveDescriptorDao.store(any())).then(invocation -> Mono.just(invocation.getArgument(0)));

        Descriptor retrievedOrCreated = service.retrieveByCoordinatesOrCreate(owned).block();

        //noinspection UnassignedFluxMonoInstance
        verify(reactiveDescriptorDao).store(descriptorCaptor.capture());
        Descriptor savedDescriptor = descriptorCaptor.getValue();
        assertThat(retrievedOrCreated, is(sameInstance(savedDescriptor)));

        assertThat(savedDescriptor.getType(), is(Descriptor.Type.COLLECTION));
        assertThat(savedDescriptor.getCollection(), is(sameInstance(owned)));
    }

    @Test
    void givenACollectionDescriptorExistsWithSuchCoordinates_whenRetrievingByCoordinatesOrCreating_thenDescriptorShouldBeSavedViaDao() {
        when(reactiveDescriptorDao.store(any()))
                .thenReturn(Mono.error(new DuplicateKeyException("such coordinatesString already exists")));
        when(reactiveDescriptorDao.retrieveByCollectionCoordinates(anyString()))
                .thenReturn(Mono.just(collDescriptorInDb));

        Descriptor retrievedOrCreated = service.retrieveByCoordinatesOrCreate(owned).block();

        assertThat(retrievedOrCreated, is(sameInstance(collDescriptorInDb)));
    }
}