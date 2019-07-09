package com.extremum.jpa.facilities;

import com.extremum.sharedmodels.descriptor.Descriptor;
import com.extremum.common.descriptor.factory.DescriptorFactory;
import com.extremum.common.descriptor.factory.DescriptorSaver;
import com.extremum.common.descriptor.factory.impl.UUIDDescriptorFacilities;

public final class PostgresqlDescriptorFacilitiesImpl extends UUIDDescriptorFacilities
        implements PostgresqlDescriptorFacilities {
    public PostgresqlDescriptorFacilitiesImpl(DescriptorFactory descriptorFactory, DescriptorSaver descriptorSaver) {
        super(descriptorFactory, descriptorSaver);
    }

    @Override
    protected Descriptor.StorageType storageType() {
        return Descriptor.StorageType.POSTGRES;
    }
}
