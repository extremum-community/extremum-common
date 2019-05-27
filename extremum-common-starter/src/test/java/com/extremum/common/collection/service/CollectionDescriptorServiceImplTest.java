package com.extremum.common.collection.service;

import com.extremum.common.collection.CollectionDescriptor;
import com.extremum.common.collection.dao.CollectionDescriptorDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
public class CollectionDescriptorServiceImplTest {
    @InjectMocks
    private CollectionDescriptorServiceImpl collectionDescriptorService;

    @Mock
    private CollectionDescriptorDao collectionDescriptorDao;

    private final CollectionDescriptor collectionDescriptor = new CollectionDescriptor("test");

    @Test
    public void whenRetrievingByExternalId_thenRetrieveFromDaoShouldBeCalled() {
        when(collectionDescriptorDao.retrieveByExternalId("externalId"))
                .thenReturn(Optional.of(collectionDescriptor));

        Optional<CollectionDescriptor> result = collectionDescriptorService.retrieveByExternalId("externalId");
        
        assertThat(result.isPresent(), is(true));
        assertThat(result.get(), is(collectionDescriptor));
    }

    @Test
    public void whenRetrievingByCoordinates_thenRetrieveFromDaoShouldBeCalled() {
        when(collectionDescriptorDao.retrieveByCoordinates("coords"))
                .thenReturn(Optional.of(collectionDescriptor));

        Optional<CollectionDescriptor> result = collectionDescriptorService.retrieveByCoordinates("coords");

        assertThat(result.isPresent(), is(true));
        assertThat(result.get(), is(collectionDescriptor));
    }

    @Test
    public void whenStoring_thenStoreInDaoShouldBeCalled() {
        collectionDescriptorService.store(collectionDescriptor);

        verify(collectionDescriptorDao).store(collectionDescriptor);
    }
}