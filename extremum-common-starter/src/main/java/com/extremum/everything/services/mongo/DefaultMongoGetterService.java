package com.extremum.everything.services.mongo;

import com.extremum.common.models.MongoCommonModel;
import com.extremum.common.service.MongoCommonService;
import com.extremum.everything.services.GetterService;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class DefaultMongoGetterService<M extends MongoCommonModel> implements GetterService<M>, DefaultMongoService<M> {
    private final List<MongoCommonService<? extends M>> services;

    @Override
    public M get(String id) {
        return getById(services, id);
    }

    @Override
    public String getSupportedModel() {
        return null;
    }
}
