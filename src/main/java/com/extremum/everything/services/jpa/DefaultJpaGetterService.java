package com.extremum.everything.services.jpa;

import com.extremum.common.models.PostgresCommonModel;
import com.extremum.common.service.PostgresCommonService;
import com.extremum.everything.services.GetterService;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class DefaultJpaGetterService<M extends PostgresCommonModel> implements GetterService<M>, DefaultJpaService<M> {
    private final List<PostgresCommonService<? extends M>> services;

    @Override
    public M get(String id) {
        return getById(services, id);
    }

    @Override
    public String getSupportedModel() {
        return null;
    }
}
