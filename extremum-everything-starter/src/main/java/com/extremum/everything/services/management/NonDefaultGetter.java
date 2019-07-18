package com.extremum.everything.services.management;

import com.extremum.common.models.Model;
import com.extremum.everything.services.GetterService;

/**
 * Uses GetterService to get an entity.
 *
 * @author rpuch
 */
final class NonDefaultGetter<M extends Model> implements Getter<M> {
    private final GetterService<M> getterService;

    NonDefaultGetter(GetterService<M> getterService) {
        this.getterService = getterService;
    }

    @Override
    public M get(String id) {
        return getterService.get(id);
    }
}
