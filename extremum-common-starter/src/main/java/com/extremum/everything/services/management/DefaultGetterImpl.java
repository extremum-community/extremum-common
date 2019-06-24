package com.extremum.everything.services.management;

import com.extremum.common.models.Model;
import com.extremum.common.service.CommonService;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class DefaultGetterImpl<M extends Model> implements DefaultGetter<M> {
    private final List<CommonService<?, ? extends M>> services;

    @Override
    public M get(String id) {
        return getById(services, id);
    }
}
