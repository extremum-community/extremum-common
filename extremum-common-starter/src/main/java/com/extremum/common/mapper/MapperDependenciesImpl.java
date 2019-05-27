package com.extremum.common.mapper;

import com.extremum.common.collection.service.CollectionDescriptorService;
import lombok.RequiredArgsConstructor;

/**
 * @author rpuch
 */
@RequiredArgsConstructor
public class MapperDependenciesImpl implements MapperDependencies {
    private final CollectionDescriptorService collectionDescriptorService;

    @Override
    public CollectionDescriptorService collectionDescriptorService() {
        return collectionDescriptorService;
    }
}
