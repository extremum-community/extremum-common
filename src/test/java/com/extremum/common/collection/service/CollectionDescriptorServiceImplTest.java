package com.extremum.common.collection.service;

import com.extremum.common.collection.CollectionDescriptor;
import com.extremum.common.collection.dao.CollectionDescriptorDao;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author rpuch
 */
@RunWith(MockitoJUnitRunner.class)
public class CollectionDescriptorServiceImplTest {
    @InjectMocks
    private CollectionDescriptorServiceImpl collectionDescriptorService;

    @Mock
    private CollectionDescriptorDao collectionDescriptorDao;

    private final CollectionDescriptor collectionDescriptor = new CollectionDescriptor("test");

    @Test
    public void whenRetrievingByCoordinates_thenRetriveFromDaoShouldBeCalled() {
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