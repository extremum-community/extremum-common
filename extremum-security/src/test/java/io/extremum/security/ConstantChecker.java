package io.extremum.security;

import io.extremum.common.model.Model;

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
