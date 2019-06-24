package com.extremum.everything.support;

import com.extremum.common.models.Model;
import com.extremum.common.service.CommonService;
import com.google.common.collect.ImmutableMap;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author rpuch
 */
public class ListBasedCommonServices implements CommonServices {
    private final Map<Class<? extends Model>, CommonService<?, ? extends Model>> modelClassToServiceMap;

    public ListBasedCommonServices(List<CommonService<?, ? extends Model>> services) {
        Map<Class<? extends Model>, CommonService<?, ? extends Model>> map = new HashMap<>();
        for (CommonService<?, ? extends Model> service : services)  {
            Class<? extends Model> modelClass = CommonServiceUtils.findServiceModelClass(service);
            map.put(modelClass, service);
        }

        modelClassToServiceMap = ImmutableMap.copyOf(map);
    }

    public <ID extends Serializable, M extends Model> CommonService<ID, M> findServiceByModel(
            Class<? extends M> modelClass) {
        @SuppressWarnings("unchecked")
        CommonService<ID, M> service = (CommonService<ID, M>) modelClassToServiceMap.get(modelClass);
        if (service == null) {
            throw new RuntimeException("Cannot find implementation of CommonService for model " + modelClass);
        }
        return service;
    }
}
