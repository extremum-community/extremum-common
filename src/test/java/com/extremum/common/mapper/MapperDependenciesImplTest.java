package com.extremum.common.mapper;

import com.extremum.common.collection.service.CollectionDescriptorService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author rpuch
 */
@RunWith(MockitoJUnitRunner.class)
public class MapperDependenciesImplTest {
    @InjectMocks
    private MapperDependenciesImpl mapperDependencies;

    @Mock
    private CollectionDescriptorService collectionDescriptorService;

    @Test
    public void collectionDescriptorServiceShouldBeReturned() {
        assertThat(mapperDependencies.collectionDescriptorService(), is(collectionDescriptorService));
    }
}