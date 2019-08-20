package io.extremum.common.descriptor.service;

import io.extremum.common.descriptor.dao.ReactiveDescriptorDao;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ReactiveDescriptorServiceImpl implements ReactiveDescriptorService {
    private final ReactiveDescriptorDao reactiveDescriptorDao;

    @Override
    public Mono<Descriptor> loadByInternalId(String internalId) {
        return reactiveDescriptorDao.retrieveByInternalId(internalId);
    }
}
