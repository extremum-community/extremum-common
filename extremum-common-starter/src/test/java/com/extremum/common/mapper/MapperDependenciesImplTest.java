package com.extremum.common.mapper;

import com.extremum.common.collection.service.CollectionDescriptorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
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