package com.extremum.everything.services.management;

import com.extremum.common.models.Model;
import com.extremum.everything.services.SaverService;

/**
 * Uses SaverService to save an entity.
 *
 * @author rpuch
 */
final class NonDefaultSaver<M extends Model> implements Saver<M> {
    private final SaverService<M> saverService;

    NonDefaultSaver(SaverService<M> saverService) {
        this.saverService = saverService;
    }

    @Override
    public M save(M model) {
        return saverService.save(model);
    }
}
