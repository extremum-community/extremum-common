package com.extremum.jpa.factory;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.factory.DescriptorFactory;
import com.extremum.common.descriptor.factory.impl.UUIDDescriptorFactory;

public final class PostgresqlDescriptorFactory extends UUIDDescriptorFactory {
    public PostgresqlDescriptorFactory(DescriptorFactory descriptorFactory) {
        super(descriptorFactory);
    }

    @Override
    public Descriptor.StorageType storageType() {
        return Descriptor.StorageType.POSTGRES;
    }
}
