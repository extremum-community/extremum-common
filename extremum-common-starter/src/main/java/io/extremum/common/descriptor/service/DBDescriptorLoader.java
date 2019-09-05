package io.extremum.common.descriptor.service;

import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.DescriptorLoader;
import reactor.core.publisher.Mono;

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

    @Override
    public Mono<Descriptor> loadByExternalIdReactively(String externalId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Mono<Descriptor> loadByInternalIdReactively(String internalId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
