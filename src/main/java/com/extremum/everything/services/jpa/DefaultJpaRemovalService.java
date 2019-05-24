package com.extremum.everything.services.jpa;

import com.extremum.common.models.PostgresBasicModel;
import com.extremum.common.service.PostgresBasicService;
import com.extremum.everything.services.RemovalService;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class DefaultJpaRemovalService<M extends PostgresBasicModel> implements RemovalService, DefaultJpaService<M> {
    private final List<PostgresBasicService<? extends M>> services;

    @Override
    public void remove(String id) {
        findServiceByModel(services, getModelByDescriptorId(id)).delete(id);
    }

    @Override
    public String getSupportedModel() {
        return null;
    }
}
