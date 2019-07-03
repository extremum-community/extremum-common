package com.extremum.elasticsearch.factory;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.factory.DescriptorSaver;
import com.extremum.common.descriptor.factory.impl.UUIDDescriptorFacilities;

public final class ElasticsearchDescriptorFacilities extends UUIDDescriptorFacilities {
    public ElasticsearchDescriptorFacilities(DescriptorSaver descriptorSaver) {
        super(descriptorSaver);
    }

    @Override
    public Descriptor.StorageType storageType() {
        return Descriptor.StorageType.ELASTICSEARCH;
    }
}