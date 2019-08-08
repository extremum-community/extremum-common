package com.extremum.watch.processor;

import com.extremum.common.models.annotation.ModelName;
import com.extremum.sharedmodels.annotation.CapturedModel;
import lombok.Getter;
import lombok.Setter;

/**
 * @author rpuch
 */
@Getter
@Setter
@CapturedModel
@ModelName(ProcessorTests.WATCHED_MODEL_NAME)
class WatchedModel extends FilledModel {
    private String name;

    WatchedModel() {
        setUuid(ProcessorTests.descriptor(getId(), ProcessorTests.WATCHED_MODEL_NAME));
    }
}
