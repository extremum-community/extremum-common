package io.extremum.common.collection.service;

import io.extremum.common.collection.dao.CollectionDescriptorDao;
import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    private CollectionDescriptorDao collectionDescriptorDao;

//    private final CollectionDescriptor collectionDescriptor = new CollectionDescriptor("test");
    private final Descriptor collDescriptorInDb = Descriptor.forCollection(CollectionDescriptor.forOwned(
        new Descriptor("host-id"), "attribute"
    ));

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
        throw new UnsupportedOperationException("Not implemented yet");

//        when(collectionDescriptorDao.retrieveByCoordinates("coords"))
//                .thenReturn(Optional.of(collectionDescriptor));
//
//        Optional<CollectionDescriptor> result = collectionDescriptorService.retrieveByCoordinates("coords");
//
//        assertThat(result.isPresent(), is(true));
//        assertThat(result.get(), is(collectionDescriptor));
    }
}