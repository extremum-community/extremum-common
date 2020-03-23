package io.extremum.mongo.service.lifecycle;

import io.extremum.mongo.facilities.ReactiveMongoDescriptorFacilities;
import io.extremum.mongo.model.MongoVersionedModel;

/**
 * @author rpuch
 */
public final class ReactiveMongoVersionedModelLifecycleListener
        extends ReactiveMongoLifecycleListener<MongoVersionedModel> {
    public ReactiveMongoVersionedModelLifecycleListener(ReactiveMongoDescriptorFacilities mongoDescriptorFacilities) {
        super(mongoDescriptorFacilities, new MongoInternalIdAdapter());
    }
}
