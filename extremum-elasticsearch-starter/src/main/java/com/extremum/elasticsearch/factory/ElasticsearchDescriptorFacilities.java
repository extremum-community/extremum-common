package com.extremum.elasticsearch.factory;

import com.extremum.sharedmodels.descriptor.Descriptor;
import com.extremum.common.descriptor.factory.DescriptorFactory;
import com.extremum.common.descriptor.factory.DescriptorSaver;
import com.extremum.common.descriptor.factory.impl.UUIDDescriptorFacilities;

public final class ElasticsearchDescriptorFacilities extends UUIDDescriptorFacilities {
    public ElasticsearchDescriptorFacilities(DescriptorFactory descriptorFactory, DescriptorSaver descriptorSaver) {
        super(descriptorFactory, descriptorSaver);
    }

    @Override
    protected Descriptor.StorageType storageType() {
        return Descriptor.StorageType.ELASTICSEARCH;
    }
}