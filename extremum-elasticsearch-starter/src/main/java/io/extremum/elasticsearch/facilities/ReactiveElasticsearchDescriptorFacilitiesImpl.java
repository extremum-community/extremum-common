package io.extremum.elasticsearch.facilities;

import io.extremum.common.descriptor.factory.ReactiveDescriptorSaver;
import io.extremum.common.descriptor.factory.impl.ReactiveUUIDDescriptorFacilities;
import io.extremum.sharedmodels.descriptor.StandardStorageType;
import io.extremum.sharedmodels.descriptor.StorageType;

public final class ReactiveElasticsearchDescriptorFacilitiesImpl extends ReactiveUUIDDescriptorFacilities
        implements ReactiveElasticsearchDescriptorFacilities {
    public ReactiveElasticsearchDescriptorFacilitiesImpl(ReactiveDescriptorSaver descriptorSaver) {
        super(descriptorSaver);
    }

    @Override
    protected StorageType storageType() {
        return StandardStorageType.ELASTICSEARCH;
    }
}