package com.extremum.watch.controller;

import com.extremum.common.models.MongoCommonModel;
import com.extremum.common.models.annotation.ModelName;
import com.extremum.sharedmodels.descriptor.Descriptor;
import org.bson.types.ObjectId;

import java.time.ZonedDateTime;

/**
 * @author rpuch
 */
@ModelName("ModelWithExpectedValues")
public class ModelWithFilledValues extends MongoCommonModel {
    public ModelWithFilledValues() {
        setId(new ObjectId());
        setUuid(Descriptor.builder()
                .externalId("external-id")
                .internalId(getId().toString())
                .build());
        setCreated(ZonedDateTime.now());
        setModified(ZonedDateTime.now());
        setVersion(1L);
    }
}
