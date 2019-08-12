package io.extremum.everything.services.management;

import io.extremum.common.models.Model;
import io.extremum.everything.services.GetterService;

/**
 * Uses GetterService to get an entity.
 *
 * @author rpuch
 */
final class NonDefaultGetter implements Getter {
    private final GetterService<?> getterService;

    NonDefaultGetter(GetterService<?> getterService) {
        this.getterService = getterService;
    }

    @Override
    public Model get(String id) {
        return getterService.get(id);
    }
}
