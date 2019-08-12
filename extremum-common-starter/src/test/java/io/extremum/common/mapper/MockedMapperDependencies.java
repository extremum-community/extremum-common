package io.extremum.common.mapper;

import io.extremum.common.collection.service.CollectionDescriptorService;
import io.extremum.common.descriptor.factory.DescriptorFactory;

import static org.mockito.Mockito.mock;

/**
 * @author rpuch
 */
public class MockedMapperDependencies implements MapperDependencies {
    private final DescriptorFactory descriptorFactory = new DescriptorFactory();
    private final CollectionDescriptorService collectionDescriptorService = mock(CollectionDescriptorService.class);

    @Override
    public DescriptorFactory descriptorFactory() {
        return descriptorFactory;
    }

    @Override
    public CollectionDescriptorService collectionDescriptorService() {
        return collectionDescriptorService;
    }
}
