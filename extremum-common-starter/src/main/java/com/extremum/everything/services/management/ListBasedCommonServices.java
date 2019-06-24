package com.extremum.everything.services.management;

import com.extremum.common.models.Model;
import com.extremum.common.service.CommonService;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author rpuch
 */
@RequiredArgsConstructor
public class ListBasedCommonServices implements CommonServices {
    private final List<CommonService<?, ? extends Model>> services;

    public <ID extends Serializable, M extends Model> CommonService<ID, M> findServiceByModel(
            Class<? extends M> modelClass) {
        @SuppressWarnings("unchecked")
        CommonService<ID, M> result = (CommonService<ID, M>) services.stream()
                .filter(service -> CommonServiceUtils.isCommonServiceOfModelClass(service, modelClass))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "Cannot find implementation of CommonService for model " + modelClass));
        return result;
    }
}
