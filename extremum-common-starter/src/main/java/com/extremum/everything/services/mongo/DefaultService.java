package com.extremum.everything.services.mongo;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.exceptions.DescriptorNotFoundException;
import com.extremum.common.descriptor.service.DescriptorService;
import com.extremum.common.models.Model;
import com.extremum.common.service.CommonService;
import com.extremum.everything.config.listener.ModelClasses;

import java.util.List;

public interface DefaultService<M extends Model> {
    default CommonService<?, ? extends M> findServiceByModel(List<CommonService<?, ? extends M>> services,
            Class<? extends Model> modelClass) {
        return services.stream()
                .filter(service -> CommonServiceUtils.isCommonServiceOfModelClass(service, modelClass))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "Cannot find implementation of CommonService for model " + modelClass));
    }

    default M getById(List<CommonService<?, ? extends M>> services, String internalId) {
        CommonService<?, ? extends M> service = findServiceByModel(services, getModelByDescriptorId(internalId));
        return service.get(internalId);
    }

    default Class<? extends Model> getModelByDescriptorId(String internalId) {
        Descriptor descriptor = DescriptorService.loadByInternalId(internalId)
                .orElseThrow(() -> new DescriptorNotFoundException("For internal id: " + internalId));

        return ModelClasses.getClassByModelName(descriptor.getModelType());
    }
}
