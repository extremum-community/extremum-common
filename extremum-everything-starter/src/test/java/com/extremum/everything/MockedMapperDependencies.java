package com.extremum.everything;

import com.extremum.common.collection.service.CollectionDescriptorService;
import com.extremum.common.descriptor.factory.DescriptorFactory;
import com.extremum.common.mapper.MapperDependencies;

import static org.mockito.Mockito.mock;

/**
 * @author rpuch
 */
public class MockedMapperDependencies implements MapperDependencies {
    private final CollectionDescriptorService collectionDescriptorService = mock(CollectionDescriptorService.class);

    @Override
    public DescriptorFactory descriptorFactory() {
        return new DescriptorFactory();
    }

    @Override
    public CollectionDescriptorService collectionDescriptorService() {
        return collectionDescriptorService;
    }
}
