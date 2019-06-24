package com.extremum.everything.services.management;

import com.extremum.common.models.Model;
import com.extremum.everything.config.listener.ModelClasses;
import com.extremum.everything.services.Remover;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultRemover<M extends Model> implements Remover {
    private final CommonServices commonServices;
    private final ModelClasses modelClasses;

    @Override
    public void remove(String id) {
        Class<? extends M> model = (Class<M>) modelClasses.getModelByDescriptorId(id);
        commonServices.findServiceByModel(model).delete(id);
    }
}
