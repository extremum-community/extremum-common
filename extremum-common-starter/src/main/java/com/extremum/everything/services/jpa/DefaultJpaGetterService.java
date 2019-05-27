package com.extremum.everything.services.jpa;

import com.extremum.common.models.PostgresBasicModel;
import com.extremum.common.service.PostgresBasicService;
import com.extremum.everything.services.GetterService;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class DefaultJpaGetterService<M extends PostgresBasicModel> implements GetterService<M>, DefaultJpaService<M> {
    private final List<PostgresBasicService<? extends M>> services;

    @Override
    public M get(String id) {
        return getById(services, id);
    }

    @Override
    public String getSupportedModel() {
        return null;
    }
}
