package io.extremum.security.services;

import io.extremum.common.models.Model;
import io.extremum.common.modelservices.ModelService;
import io.extremum.security.CheckerContext;

/**
 * @author rpuch
 */
public interface DataAccessChecker<M extends Model> extends ModelService {
    boolean allowedToGet(M model, CheckerContext context);

    boolean allowedToPatch(M model, CheckerContext context);

    boolean allowedToRemove(M model, CheckerContext context);

    boolean allowedToWatch(M model, CheckerContext context);
}
