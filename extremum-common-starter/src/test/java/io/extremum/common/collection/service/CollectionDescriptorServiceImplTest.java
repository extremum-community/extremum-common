package io.extremum.common.collection.service;

import io.extremum.common.descriptor.dao.DescriptorDao;
import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
class CollectionDescriptorServiceImplTest {
    @InjectMocks
    private CollectionDescriptorServiceImpl collectionDescriptorService;

    @Mock
    private DescriptorService descriptorService;
    @Mock
    private DescriptorDao descriptorDao;

    @Captor
    private ArgumentCaptor<Descriptor> descriptorCaptor;

    private final CollectionDescriptor owned = CollectionDescriptor.forOwned(
            new Descriptor("host-id"), "attribute");
    private final Descriptor collDescriptorInDb = Descriptor.forCollection("external-id", owned);

    @Test
    void givenDescriptorContainsCollection_whenRetrievingByExternalId_thenShouldBeRetrievedFromDescriptorService() {
        when(descriptorService.loadByExternalId("external-id")).thenReturn(Optional.of(collDescriptorInDb));

        CollectionDescriptor collectionDescriptor = collectionDescriptorService.retrieveByExternalId("external-id")
                .orElse(null);

        assertThat(collectionDescriptor, is(notNullValue()));
        assertThat(collectionDescriptor, is(sameInstance(collDescriptorInDb.getCollection())));
    }

    @Test
    void givenDescriptorIsNotACollection_whenRetrievingByExternalId_thenAnExceptionShouldBeThrown() {
        collDescriptorInDb.setType(Descriptor.Type.SINGLE);
        when(descriptorService.loadByExternalId("external-id")).thenReturn(Optional.of(collDescriptorInDb));

        try {
            collectionDescriptorService.retrieveByExternalId("external-id");
            Assertions.fail("An exception should be thrown");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is("Descriptor 'external-id' must have type COLLECTION, but it is 'SINGLE'"));
        }
    }

    @Test
    void givenDescriptorHasNoCollectionDescriptor_whenRetrievingByExternalId_thenAnExceptionShouldBeThrown() {
        collDescriptorInDb.setCollection(null);
        when(descriptorService.loadByExternalId("external-id")).thenReturn(Optional.of(collDescriptorInDb));

        try {
            collectionDescriptorService.retrieveByExternalId("external-id");
            Assertions.fail("An exception should be thrown");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(),
                    is("Descriptor 'external-id' has type COLLECTION, but there is no collection in it"));
        }
    }

    @Test
    void whenRetrievingByCoordinates_thenRetrieveFromDaoShouldBeCalled() {
        when(descriptorDao.retrieveByCollectionCoordinates("coords"))
                .thenReturn(Optional.of(collDescriptorInDb));

        Optional<Descriptor> result = collectionDescriptorService.retrieveByCoordinates("coords");

        assertThat(result.orElse(null), is(collDescriptorInDb));
    }

    @Test
    void givenNoCollectionDescriptorExistsWithSuchCoordinates_whenRetrievingByCoordinatesOrCreating_thenDescriptorShouldBeSavedViaDao() {
        when(descriptorDao.store(any())).then(invocation -> invocation.getArgument(0));

        Descriptor retrievedOrCreated = collectionDescriptorService.retrieveByCoordinatesOrCreate(owned);

        verify(descriptorDao).store(descriptorCaptor.capture());
        Descriptor savedDescriptor = descriptorCaptor.getValue();
        assertThat(retrievedOrCreated, is(sameInstance(savedDescriptor)));

        assertThat(savedDescriptor.getType(), is(Descriptor.Type.COLLECTION));
        assertThat(savedDescriptor.getCollection(), is(sameInstance(owned)));
    }

    @Test
    void givenACollectionDescriptorExistsWithSuchCoordinates_whenRetrievingByCoordinatesOrCreating_thenDescriptorShouldBeSavedViaDao() {
        when(descriptorDao.store(any()))
                .thenThrow(new DuplicateKeyException("such coordinatesString already exists"));
        when(descriptorDao.retrieveByCollectionCoordinates(anyString()))
                .thenReturn(Optional.of(collDescriptorInDb));

        Descriptor retrievedOrCreated = collectionDescriptorService.retrieveByCoordinatesOrCreate(owned);

        assertThat(retrievedOrCreated, is(sameInstance(collDescriptorInDb)));
    }
}