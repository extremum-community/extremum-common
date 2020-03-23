package io.extremum.mongo.service.lifecycle;

import io.extremum.mongo.facilities.ReactiveMongoDescriptorFacilities;
import io.extremum.mongo.model.MongoCommonModel;

/**
 * @author rpuch
 */
public final class ReactiveMongoCommonModelLifecycleListener
        extends ReactiveMongoLifecycleListener<MongoCommonModel> {
    public ReactiveMongoCommonModelLifecycleListener(ReactiveMongoDescriptorFacilities mongoDescriptorFacilities) {
        super(mongoDescriptorFacilities, new MongoInternalIdAdapter());
    }
}