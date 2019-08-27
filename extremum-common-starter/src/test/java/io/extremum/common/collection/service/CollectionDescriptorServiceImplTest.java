package io.extremum.common.collection.service;

import io.extremum.common.collection.dao.CollectionDescriptorDao;
import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
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
class CollectionDescriptorServiceImplTest {
    @InjectMocks
    private CollectionDescriptorServiceImpl collectionDescriptorService;

    @Mock
    private CollectionDescriptorDao collectionDescriptorDao;

    private final CollectionDescriptor collectionDescriptor = new CollectionDescriptor("test");

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