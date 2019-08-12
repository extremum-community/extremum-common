package io.extremum.elasticsearch.facilities;

import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.common.descriptor.factory.DescriptorFactory;
import io.extremum.common.descriptor.factory.DescriptorSaver;
import io.extremum.common.descriptor.factory.impl.UUIDDescriptorFacilities;

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