package com.extremum.watch.processor;

import com.extremum.common.models.MongoCommonModel;
import org.bson.types.ObjectId;

/**
 * @author rpuch
 */
abstract class FilledModel extends MongoCommonModel {
    FilledModel() {
        setId(new ObjectId());
        setCreated(ProcessorTests.CREATED);
        setModified(ProcessorTests.LAST_MODIFIED);
        setVersion(ProcessorTests.VERSION);
    }
}
