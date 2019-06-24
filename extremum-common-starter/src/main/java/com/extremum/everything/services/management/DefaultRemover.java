package com.extremum.everything.services.management;

import com.extremum.common.models.Model;
import com.extremum.everything.services.Remover;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultRemover<M extends Model> implements Remover, DefaultService<M> {
    private final CommonServices commonServices;

    @Override
    public void remove(String id) {
        Class<? extends M> model = (Class<M>) getModelByDescriptorId(id);
        commonServices.findServiceByModel(model).delete(id);
    }
}
