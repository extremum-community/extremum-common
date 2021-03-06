package io.extremum.everything.services.management;

import io.extremum.sharedmodels.basic.Model;
import io.extremum.everything.services.GetterService;

/**
 * Uses GetterService to get an entity.
 *
 * @author rpuch
 */
final class NonDefaultGetter implements Getter {
    private final GetterService<Model> getterService;

    NonDefaultGetter(GetterService<Model> getterService) {
        this.getterService = getterService;
    }

    @Override
    public Model get(String id) {
        return getterService.get(id);
    }
}
