package com.extremum.watch.processor;

import com.extremum.common.models.annotation.ModelName;
import lombok.Getter;
import lombok.Setter;

/**
 * @author rpuch
 */
@Getter
@Setter
@ModelName(ProcessorTests.NON_WATCHED_MODEL_NAME)
class NonWatchedModel extends FilledModel {
    NonWatchedModel() {
        setUuid(ProcessorTests.descriptor(getId(), ProcessorTests.NON_WATCHED_MODEL_NAME));
    }
}
