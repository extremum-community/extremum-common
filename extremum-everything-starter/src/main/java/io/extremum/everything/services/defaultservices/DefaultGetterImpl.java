package io.extremum.everything.services.defaultservices;

import io.extremum.common.models.Model;
import io.extremum.common.service.CommonService;
import io.extremum.common.support.CommonServices;
import io.extremum.everything.support.ModelDescriptors;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class DefaultGetterImpl implements DefaultGetter {
    private final CommonServices commonServices;
    private final ModelDescriptors modelDescriptors;

    @Override
    public Model get(String internalId) {
        Class<? extends Model> modelClass = modelDescriptors.getModelClassByDescriptorId(internalId);
        CommonService<?> service = commonServices.findServiceByModel(modelClass);
        return service.get(internalId);
    }

    @Override
    public Mono<Model> reactiveGet(String id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
