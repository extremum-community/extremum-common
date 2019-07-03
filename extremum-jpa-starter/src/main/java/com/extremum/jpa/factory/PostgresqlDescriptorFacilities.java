package com.extremum.jpa.factory;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.factory.DescriptorSaver;
import com.extremum.common.descriptor.factory.impl.UUIDDescriptorFacilities;

public final class PostgresqlDescriptorFacilities extends UUIDDescriptorFacilities {
    public PostgresqlDescriptorFacilities(DescriptorSaver descriptorSaver) {
        super(descriptorSaver);
    }

    @Override
    public Descriptor.StorageType storageType() {
        return Descriptor.StorageType.POSTGRES;
    }
}
