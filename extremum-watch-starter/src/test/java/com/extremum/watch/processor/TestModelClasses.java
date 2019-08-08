package com.extremum.watch.processor;

import com.extremum.common.models.Model;
import com.extremum.everything.support.ModelClasses;

/**
 * @author rpuch
 */
class TestModelClasses implements ModelClasses {
    @SuppressWarnings("unchecked")
    @Override
    public <M extends Model> Class<M> getClassByModelName(String modelName) {
        if (ProcessorTests.WATCHED_MODEL_NAME.equals(modelName)) {
            return (Class<M>) WatchedModel.class;
        }
        if (ProcessorTests.NON_WATCHED_MODEL_NAME.equals(modelName)) {
            return (Class<M>) NonWatchedModel.class;
        }
        throw new IllegalStateException(String.format("We don't know '%s'", modelName));
    }

}
