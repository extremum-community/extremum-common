package com.extremum.common.mapper;

import com.extremum.common.collection.service.CollectionDescriptorService;
import com.extremum.common.descriptor.factory.DescriptorFactory;

/**
 * @author rpuch
 */
public interface MapperDependencies {
    DescriptorFactory descriptorFactory();

    CollectionDescriptorService collectionDescriptorService();
}
