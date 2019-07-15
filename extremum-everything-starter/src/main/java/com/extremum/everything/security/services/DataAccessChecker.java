package com.extremum.everything.security.services;

import com.extremum.common.models.Model;
import com.extremum.everything.security.CheckerContext;
import com.extremum.everything.services.EverythingEverythingService;

/**
 * @author rpuch
 */
public interface DataAccessChecker<M extends Model> extends EverythingEverythingService {
    boolean allowedToGet(M model, CheckerContext context);

    boolean allowedToPatch(M model, CheckerContext context);

    boolean allowedToRemove(M model, CheckerContext context);
}
