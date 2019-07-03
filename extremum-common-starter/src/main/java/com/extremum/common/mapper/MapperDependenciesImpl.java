package com.extremum.common.mapper;

import com.extremum.common.collection.service.CollectionDescriptorService;
import com.extremum.common.descriptor.factory.DescriptorFactory;
import lombok.RequiredArgsConstructor;

/**
 * @author rpuch
 */
@RequiredArgsConstructor
public class MapperDependenciesImpl implements MapperDependencies {
    private final DescriptorFactory descriptorFactory;
    private final CollectionDescriptorService collectionDescriptorService;

    @Override
    public DescriptorFactory descriptorFactory() {
        return descriptorFactory;
    }

    @Override
    public CollectionDescriptorService collectionDescriptorService() {
        return collectionDescriptorService;
    }
}
