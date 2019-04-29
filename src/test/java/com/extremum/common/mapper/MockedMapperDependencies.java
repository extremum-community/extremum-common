package com.extremum.common.mapper;

import com.extremum.common.collection.service.CollectionDescriptorService;

import static org.mockito.Mockito.mock;

/**
 * @author rpuch
 */
public class MockedMapperDependencies implements MapperDependencies {
    private final CollectionDescriptorService collectionDescriptorService = mock(CollectionDescriptorService.class);

    @Override
    public CollectionDescriptorService collectionDescriptorService() {
        return collectionDescriptorService;
    }
}
