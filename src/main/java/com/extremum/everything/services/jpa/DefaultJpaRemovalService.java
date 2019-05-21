package com.extremum.everything.services.jpa;

import com.extremum.common.models.PostgresCommonModel;
import com.extremum.common.service.PostgresCommonService;
import com.extremum.everything.services.RemovalService;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class DefaultJpaRemovalService<M extends PostgresCommonModel> implements RemovalService, DefaultJpaService<M> {
    private final List<PostgresCommonService<? extends M>> services;

    @Override
    public void remove(String id) {
        findServiceByModel(services, getModelByDescriptorId(id)).delete(id);
    }

    @Override
    public String getSupportedModel() {
        return null;
    }
}
