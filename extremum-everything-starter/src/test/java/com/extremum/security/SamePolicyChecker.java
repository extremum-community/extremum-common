package com.extremum.security;

import com.extremum.common.models.Model;
import com.extremum.security.services.DataAccessChecker;
import com.extremum.security.CheckerContext;

/**
 * @author rpuch
 */
abstract class SamePolicyChecker<M extends Model> implements DataAccessChecker<M> {
    @Override
    public final boolean allowedToGet(M model, CheckerContext context) {
        return allowed(model, context);
    }

    @Override
    public final boolean allowedToPatch(M model, CheckerContext context) {
        return allowed(model, context);
    }

    @Override
    public final boolean allowedToRemove(M model, CheckerContext context) {
        return allowed(model, context);
    }

    abstract boolean allowed(M model, CheckerContext context);
}
