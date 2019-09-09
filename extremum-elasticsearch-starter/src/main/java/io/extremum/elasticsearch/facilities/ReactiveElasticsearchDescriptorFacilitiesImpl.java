package io.extremum.elasticsearch.facilities;

import io.extremum.common.descriptor.factory.ReactiveDescriptorSaver;
import io.extremum.common.descriptor.factory.impl.ReactiveUUIDDescriptorFacilities;
import io.extremum.sharedmodels.descriptor.Descriptor;

public final class ReactiveElasticsearchDescriptorFacilitiesImpl extends ReactiveUUIDDescriptorFacilities
        implements ReactiveElasticsearchDescriptorFacilities {
    public ReactiveElasticsearchDescriptorFacilitiesImpl(ReactiveDescriptorSaver descriptorSaver) {
        super(descriptorSaver);
    }

    @Override
    protected Descriptor.StorageType storageType() {
        return Descriptor.StorageType.ELASTICSEARCH;
    }
}