package com.extremum.everything.services.management;

import com.extremum.common.models.Model;
import com.extremum.common.service.CommonService;
import com.extremum.everything.services.Remover;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class DefaultRemover<M extends Model> implements Remover, DefaultService<M> {
    private final List<CommonService<?, ? extends M>> services;

    @Override
    public void remove(String id) {
        findServiceByModel(services, getModelByDescriptorId(id)).delete(id);
    }
}
