package io.extremum.everything.services.defaultservices;

import io.extremum.common.descriptor.service.ReactiveDescriptorService;
import io.extremum.common.models.Model;
import io.extremum.common.support.UniversalReactiveModelLoaders;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class DefaultReactiveGetterImpl implements DefaultReactiveGetter {
    private final ReactiveDescriptorService reactiveDescriptorService;
    private final UniversalReactiveModelLoaders universalReactiveModelLoaders;

    @Override
    public Mono<Model> reactiveGet(String internalId) {
        return reactiveDescriptorService.loadByInternalId(internalId)
                .flatMap(this::loadModelByDescriptorReactively);
    }

    private Mono<Model> loadModelByDescriptorReactively(Descriptor descriptor) {
        return universalReactiveModelLoaders.loadByDescriptor(descriptor);
    }
}
