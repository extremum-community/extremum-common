package io.extremum.everything.services.defaultservices;

import io.extremum.common.descriptor.service.ReactiveDescriptorService;
import io.extremum.common.models.Model;
import io.extremum.common.service.CommonService;
import io.extremum.common.support.CommonServices;
import io.extremum.common.support.ModelClasses;
import io.extremum.common.support.UniversalReactiveModelLoaders;
import io.extremum.everything.support.ModelDescriptors;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class DefaultGetterImpl implements DefaultGetter {
    private final CommonServices commonServices;
    private final ModelDescriptors modelDescriptors;
    private final ReactiveDescriptorService reactiveDescriptorService;
    private final UniversalReactiveModelLoaders universalReactiveModelLoaders;
    private final ModelClasses modelClasses;

    @Override
    public Model get(String internalId) {
        CommonService<Model> service = findService(internalId);
        return service.get(internalId);
    }

    private CommonService<Model> findService(String internalId) {
        Class<Model> modelClass = modelDescriptors.getModelClassByModelInternalId(internalId);
        return commonServices.findServiceByModel(modelClass);
    }

    @Override
    public Mono<Model> reactiveGet(String internalId) {
        return reactiveDescriptorService.loadByInternalId(internalId)
                .flatMap(this::loadModelByDescriptorReactively);
    }

    private Mono<Model> loadModelByDescriptorReactively(Descriptor descriptor) {
        Class<Model> modelClass = modelClasses.getClassByModelName(descriptor.getModelType());
        return universalReactiveModelLoaders.findLoader(descriptor)
                .loadByInternalId(descriptor.getInternalId(), modelClass);
    }
}
