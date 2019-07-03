package com.extremum.everything.services.defaultservices;

import com.extremum.common.models.Model;
import com.extremum.common.service.CommonService;
import com.extremum.everything.support.CommonServices;
import com.extremum.everything.support.ModelDescriptors;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultGetterImpl<M extends Model> implements DefaultGetter<M> {
    private final CommonServices commonServices;
    private final ModelDescriptors modelDescriptors;

    @Override
    public M get(String internalId) {
        Class<M> modelClass = modelDescriptors.getModelClassByDescriptorId(internalId);
        CommonService<? extends M> service = commonServices.findServiceByModel(modelClass);
        return service.get(internalId);
    }
}
