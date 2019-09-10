package io.extremum.common.descriptor.service;

import io.extremum.common.descriptor.dao.ReactiveDescriptorDao;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.Objects;

@RequiredArgsConstructor
public class ReactiveDescriptorServiceImpl implements ReactiveDescriptorService {
    private final ReactiveDescriptorDao reactiveDescriptorDao;

    @Override
    public Mono<Descriptor> store(Descriptor descriptor) {
        Objects.requireNonNull(descriptor, "Descriptor is null");

        return reactiveDescriptorDao.store(descriptor);
    }

    @Override
    public Mono<Descriptor> loadByExternalId(String externalId) {
        Objects.requireNonNull(externalId, "externalId is null");

        return reactiveDescriptorDao.retrieveByExternalId(externalId);
    }

    @Override
    public Mono<Descriptor> loadByInternalId(String internalId) {
        Objects.requireNonNull(internalId, "internalId is null");

        return reactiveDescriptorDao.retrieveByInternalId(internalId);
    }
}
