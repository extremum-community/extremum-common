package com.extremum.everything.security;

import com.extremum.common.models.Model;
import com.extremum.everything.security.services.DataAccessChecker;

/**
 * @author rpuch
 */
abstract class ConstantChecker<M extends Model> implements DataAccessChecker<M> {
    private final boolean valueToReturn;

    ConstantChecker(boolean valueToReturn) {
        this.valueToReturn = valueToReturn;
    }

    @Override
    public boolean allowedToGet(M model, CheckerContext context) {
        return valueToReturn;
    }

    @Override
    public boolean allowedToPatch(M model, CheckerContext context) {
        return valueToReturn;
    }

    @Override
    public boolean allowedToRemove(M model, CheckerContext context) {
        return valueToReturn;
    }
}
