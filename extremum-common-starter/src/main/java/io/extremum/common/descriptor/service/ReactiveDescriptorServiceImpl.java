package io.extremum.common.descriptor.service;

import io.extremum.common.descriptor.dao.ReactiveDescriptorDao;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.DescriptorNotReadyException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

@RequiredArgsConstructor
public class ReactiveDescriptorServiceImpl implements ReactiveDescriptorService {
    private final ReactiveDescriptorDao reactiveDescriptorDao;

    private final DescriptorReadinessValidation descriptorReadinessValidation = new DescriptorReadinessValidation();

    @Override
    public Mono<Descriptor> store(Descriptor descriptor) {
        Objects.requireNonNull(descriptor, "Descriptor is null");

        return reactiveDescriptorDao.store(descriptor);
    }

    @Override
    public Mono<Descriptor> loadByExternalId(String externalId) {
        Objects.requireNonNull(externalId, "externalId is null");

        return reactiveDescriptorDao.retrieveByExternalId(externalId)
                .map(descriptor -> {
                    if (descriptor.getReadiness() == Descriptor.Readiness.BLANK) {
                        throw new DescriptorNotReadyException(
                                String.format("Descriptor with external ID '%s' is not ready yet", externalId));
                    }
                    return descriptor;
                });
    }

    @Override
    public Mono<Descriptor> loadByInternalId(String internalId) {
        Objects.requireNonNull(internalId, "internalId is null");

        return reactiveDescriptorDao.retrieveByInternalId(internalId);
    }

    @Override
    public Mono<Map<String, String>> loadMapByInternalIds(Collection<String> internalIds) {
        return reactiveDescriptorDao.retrieveMapByInternalIds(internalIds);
    }

    @Override
    public Mono<Descriptor> makeDescriptorReady(String descriptorExternalId, String modelType) {
        return reactiveDescriptorDao.retrieveByExternalId(descriptorExternalId)
                .doOnNext(descriptor -> descriptorReadinessValidation.validateDescriptorIsNotReady(
                        descriptorExternalId, descriptor))
                .doOnNext(descriptor -> {
                    descriptor.setReadiness(Descriptor.Readiness.READY);
                    descriptor.setModelType(modelType);
                })
                .flatMap(reactiveDescriptorDao::store);
    }
}
