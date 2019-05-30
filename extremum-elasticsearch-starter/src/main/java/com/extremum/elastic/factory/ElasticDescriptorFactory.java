package com.extremum.elastic.factory;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.factory.impl.UUIDDescriptorFactory;
import org.springframework.stereotype.Component;

@Component
public final class ElasticDescriptorFactory extends UUIDDescriptorFactory {
    @Override
    public Descriptor.StorageType storageType() {
        return Descriptor.StorageType.ELASTIC;
    }
}