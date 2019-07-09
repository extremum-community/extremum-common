package com.extremum.elasticsearch.facilities;

import com.extremum.sharedmodels.descriptor.Descriptor;
import com.extremum.common.descriptor.factory.DescriptorFactory;
import com.extremum.common.descriptor.factory.DescriptorSaver;
import com.extremum.common.descriptor.factory.impl.UUIDDescriptorFacilities;

public final class ElasticsearchDescriptorFacilitiesImpl extends UUIDDescriptorFacilities
        implements ElasticsearchDescriptorFacilities {
    public ElasticsearchDescriptorFacilitiesImpl(DescriptorFactory descriptorFactory, DescriptorSaver descriptorSaver) {
        super(descriptorFactory, descriptorSaver);
    }

    @Override
    protected Descriptor.StorageType storageType() {
        return Descriptor.StorageType.ELASTICSEARCH;
    }
}