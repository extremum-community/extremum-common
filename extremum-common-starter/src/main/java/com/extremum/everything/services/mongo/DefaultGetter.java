package com.extremum.everything.services.mongo;

import com.extremum.common.models.Model;
import com.extremum.common.service.CommonService;
import com.extremum.everything.services.management.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class DefaultGetter<M extends Model> implements Getter<M>, DefaultService<M> {
    private final List<CommonService<?, ? extends M>> services;

    @Override
    public M get(String id) {
        return getById(services, id);
    }
}
