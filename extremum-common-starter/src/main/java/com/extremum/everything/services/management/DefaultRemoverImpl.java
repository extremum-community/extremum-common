package com.extremum.everything.services.management;

import com.extremum.common.models.Model;
import com.extremum.common.service.CommonService;
import com.extremum.everything.support.CommonServices;
import com.extremum.everything.support.ModelDescriptors;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultRemoverImpl implements DefaultRemover {
    private final CommonServices commonServices;
    private final ModelDescriptors modelDescriptors;

    @Override
    public void remove(String id) {
        Class<? extends Model> modelClass = modelDescriptors.getModelClassByDescriptorId(id);
        CommonService<? extends Model> service = commonServices.findServiceByModel(modelClass);
        service.delete(id);
    }

}
