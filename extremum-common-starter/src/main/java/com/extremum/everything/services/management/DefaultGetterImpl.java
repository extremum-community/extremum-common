package com.extremum.everything.services.management;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.exceptions.DescriptorNotFoundException;
import com.extremum.common.descriptor.service.DescriptorService;
import com.extremum.common.models.Model;
import com.extremum.common.service.CommonService;
import com.extremum.everything.support.CommonServices;
import com.extremum.everything.support.ModelClasses;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultGetterImpl<M extends Model> implements DefaultGetter<M> {
    private final CommonServices commonServices;
    private final ModelClasses modelClasses;

    @Override
    public M get(String internalId) {
        Class<M> modelClass = getModelByDescriptorId1(internalId);
        CommonService<? extends M> service = commonServices.findServiceByModel(modelClass);
        return service.get(internalId);
    }

    private Class<M> getModelByDescriptorId1(String internalId) {
        Descriptor descriptor = DescriptorService.loadByInternalId(internalId)
                .orElseThrow(() -> new DescriptorNotFoundException("For internal id: " + internalId));

        return modelClasses.getClassByModelName(descriptor.getModelType());
    }
}
