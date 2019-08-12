package io.extremum.common.mapper;

import io.extremum.common.collection.service.CollectionDescriptorService;
import io.extremum.common.descriptor.factory.DescriptorFactory;

/**
 * @author rpuch
 */
public interface MapperDependencies {
    DescriptorFactory descriptorFactory();

    CollectionDescriptorService collectionDescriptorService();
}
