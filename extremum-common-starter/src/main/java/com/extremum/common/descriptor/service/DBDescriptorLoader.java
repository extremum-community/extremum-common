package com.extremum.common.descriptor.service;

import com.extremum.sharedmodels.descriptor.Descriptor;
import com.extremum.sharedmodels.descriptor.DescriptorLoader;

import java.util.Optional;

/**
 * @author rpuch
 */
public class DBDescriptorLoader implements DescriptorLoader {
    private final DescriptorService descriptorService;

    public DBDescriptorLoader(DescriptorService descriptorService) {
        this.descriptorService = descriptorService;
    }

    @Override
    public Optional<Descriptor> loadByExternalId(String externalId) {
        return descriptorService.loadByExternalId(externalId);
    }

    @Override
    public Optional<Descriptor> loadByInternalId(String internalId) {
        return descriptorService.loadByInternalId(internalId);
    }
}
