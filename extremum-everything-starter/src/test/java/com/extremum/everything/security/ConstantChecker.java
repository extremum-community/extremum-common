package com.extremum.everything.security;

import com.extremum.common.models.Model;
import com.extremum.everything.security.services.DataAccessChecker;

/**
 * @author rpuch
 */
abstract class ConstantChecker<M extends Model> extends SamePolicyChecker<M> {
    private final boolean valueToReturn;

    ConstantChecker(boolean valueToReturn) {
        this.valueToReturn = valueToReturn;
    }

    @Override
    final boolean allowed(M model, CheckerContext context) {
        return valueToReturn;
    }
}
