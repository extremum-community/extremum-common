package io.extremum.everything.services.defaultservices;

import io.extremum.common.model.Model;
import io.extremum.common.service.CommonService;
import io.extremum.common.support.CommonServices;
import io.extremum.everything.support.ModelDescriptors;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultGetterImpl implements DefaultGetter {
    private final CommonServices commonServices;
    private final ModelDescriptors modelDescriptors;

    @Override
    public Model get(String internalId) {
        CommonService<Model> service = findService(internalId);
        return service.get(internalId);
    }

    private CommonService<Model> findService(String internalId) {
        Class<Model> modelClass = modelDescriptors.getModelClassByModelInternalId(internalId);
        return commonServices.findServiceByModel(modelClass);
    }
}
