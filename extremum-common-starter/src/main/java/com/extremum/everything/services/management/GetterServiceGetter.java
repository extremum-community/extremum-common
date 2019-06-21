package com.extremum.everything.services.management;

import com.extremum.common.models.Model;
import com.extremum.everything.services.GetterService;

/**
 * @author rpuch
 */
class GetterServiceGetter<M extends Model> implements Getter<M> {
    private final GetterService<M> getterService;

    GetterServiceGetter(GetterService<M> getterService) {
        this.getterService = getterService;
    }

    @Override
    public M get(String id) {
        return getterService.get(id);
    }
}
