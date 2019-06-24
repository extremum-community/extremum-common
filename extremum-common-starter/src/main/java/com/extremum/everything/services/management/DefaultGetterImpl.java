package com.extremum.everything.services.management;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.exceptions.DescriptorNotFoundException;
import com.extremum.common.descriptor.service.DescriptorService;
import com.extremum.common.models.Model;
import com.extremum.common.service.CommonService;
import com.extremum.everything.config.listener.ModelClasses;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultGetterImpl<M extends Model> implements DefaultGetter<M> {
    private final CommonServices commonServices;
    private final ModelClasses modelClasses;

    @Override
    public M get(String internalId) {
        Class<? extends Model> modelClass = getModelByDescriptorId1(internalId);
        CommonService<?, ? extends M> service = (CommonService<?, ? extends M>) commonServices.findServiceByModel(
                modelClass);
        return service.get(internalId);
    }

    private Class<? extends Model> getModelByDescriptorId1(String internalId) {
        Descriptor descriptor = DescriptorService.loadByInternalId(internalId)
                .orElseThrow(() -> new DescriptorNotFoundException("For internal id: " + internalId));

        return modelClasses.getClassByModelName(descriptor.getModelType());
    }
}
