package io.extremum.common.collection.service;

import io.extremum.common.descriptor.dao.ReactiveDescriptorDao;
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
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReactiveCollectionDescriptorServiceImplTest {
    @InjectMocks
    private ReactiveCollectionDescriptorServiceImpl service;

    @Mock
    private ReactiveDescriptorDao reactiveDescriptorDao;

    private final Descriptor collectionDescriptor = Descriptor.forCollection("externalId",
            CollectionDescriptor.forOwned(new Descriptor("host-id"), "items")
    );

    @Test
    void whenRetrievingACollectionByExternalId_thenItShouldBeRetrievedFromDao() {
        when(reactiveDescriptorDao.retrieveByExternalId("externalId"))
                .thenReturn(Mono.just(collectionDescriptor));

        Mono<CollectionDescriptor> mono = service.retrieveByExternalId("externalId");

        assertThat(mono.block(), is(sameInstance(collectionDescriptor.getCollection())));
    }

    @Test
    void givenDescriptorTypeIsNotCollection_whenRetrievingACollectionByExternalId_thenItShouldBeRetrievedFromDao() {
        collectionDescriptor.setType(Descriptor.Type.SINGLE);
        when(reactiveDescriptorDao.retrieveByExternalId("externalId"))
                .thenReturn(Mono.just(collectionDescriptor));

        try {
            service.retrieveByExternalId("externalId").block();
            fail("An exception should be thrown");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is("Descriptor 'externalId' must have type COLLECTION, but it is 'SINGLE'"));
        }
    }

    @Test
    void givenDescriptorHasNoCollection_whenRetrievingACollection_thenItShouldBeRetrievedFromDao() {
        collectionDescriptor.setCollection(null);
        when(reactiveDescriptorDao.retrieveByExternalId("externalId"))
                .thenReturn(Mono.just(collectionDescriptor));

        try {
            service.retrieveByExternalId("externalId").block();
            fail("An exception should be thrown");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(),
                    is("Descriptor 'externalId' has type COLLECTION, but there is no collection in it"));
        }
    }

    @Test
    void whenRetrievingACollectionByCoordinatesString_thenItShouldBeRetrievedFromDao() {
        when(reactiveDescriptorDao.retrieveByCollectionCoordinates("coords"))
                .thenReturn(Mono.just(collectionDescriptor));

        Mono<Descriptor> mono = service.retrieveByCoordinates("coords");

        assertThat(mono.block(), is(sameInstance(collectionDescriptor)));
    }

    @Test
    void givenDescriptorTypeIsNotCollection_whenRetrievingACollectionByCoordinatesString_thenItShouldBeRetrievedFromDao() {
        collectionDescriptor.setType(Descriptor.Type.SINGLE);
        when(reactiveDescriptorDao.retrieveByCollectionCoordinates("coords"))
                .thenReturn(Mono.just(collectionDescriptor));

        try {
            service.retrieveByCoordinates("coords").block();
            fail("An exception should be thrown");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is("Descriptor 'externalId' must have type COLLECTION, but it is 'SINGLE'"));
        }
    }
}