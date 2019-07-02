package com.extremum.elasticsearch.factory;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.factory.DescriptorSaver;
import com.extremum.common.descriptor.factory.impl.UUIDDescriptorFactory;

public final class ElasticsearchDescriptorFactory extends UUIDDescriptorFactory {
    public ElasticsearchDescriptorFactory(DescriptorSaver descriptorSaver) {
        super(descriptorSaver);
    }

    @Override
    public Descriptor.StorageType storageType() {
        return Descriptor.StorageType.ELASTICSEARCH;
    }
}