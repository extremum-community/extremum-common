package com.extremum.common.descriptor.factory.impl;

import com.extremum.common.descriptor.Descriptor;
import org.springframework.stereotype.Component;

@Component
public final class ElasticDescriptorFactory extends UUIDDescriptorFactory {
    @Override
    Descriptor.StorageType storageType() {
        return Descriptor.StorageType.ELASTIC;
    }
}