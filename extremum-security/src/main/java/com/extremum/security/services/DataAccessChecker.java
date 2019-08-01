package com.extremum.security.services;

import com.extremum.common.models.Model;
import com.extremum.common.modelservices.ModelService;
import com.extremum.security.CheckerContext;

/**
 * @author rpuch
 */
public interface DataAccessChecker<M extends Model> extends ModelService {
    boolean allowedToGet(M model, CheckerContext context);

    boolean allowedToPatch(M model, CheckerContext context);

    boolean allowedToRemove(M model, CheckerContext context);
}
