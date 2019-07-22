package com.extremum.everything.services.defaultservices;

import com.extremum.common.models.Model;
import com.extremum.common.service.CommonService;
import com.extremum.everything.support.CommonServices;
import com.extremum.everything.support.ModelDescriptors;
import lombok.RequiredArgsConstructor;

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
}
