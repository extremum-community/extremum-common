package com.extremum.everything.services.mongo;

import com.extremum.common.models.MongoCommonModel;
import com.extremum.elastic.service.MongoCommonService;
import com.extremum.everything.services.RemovalService;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class DefaultMongoRemovalService<M extends MongoCommonModel> implements RemovalService, DefaultMongoService<M> {
    private final List<MongoCommonService<? extends M>> services;

    @Override
    public void remove(String id) {
        findServiceByModel(services, getModelByDescriptorId(id)).delete(id);
    }

    @Override
    public String getSupportedModel() {
        return null;
    }
}
