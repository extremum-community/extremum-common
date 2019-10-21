package io.extremum.mongo.service.lifecycle;

import io.extremum.mongo.facilities.ReactiveMongoDescriptorFacilities;
import io.extremum.mongo.model.MongoVersionedModel;
import io.extremum.mongo.springdata.ReactiveAfterConvertEvent;
import io.extremum.mongo.springdata.ReactiveAfterSaveEvent;
import io.extremum.mongo.springdata.ReactiveBeforeConvertEvent;
import io.extremum.mongo.springdata.lifecycle.AbstractReactiveMongoEventListener;
import reactor.core.publisher.Mono;

/**
 * @author rpuch
 */
public final class ReactiveMongoVersionedModelLifecycleListener
        extends AbstractReactiveMongoEventListener<MongoVersionedModel> {
    private final ReactiveMongoModelsLifecycleSupport modelsLifecycleSupport;

    public ReactiveMongoVersionedModelLifecycleListener(ReactiveMongoDescriptorFacilities mongoDescriptorFacilities) {
        modelsLifecycleSupport = new ReactiveMongoModelsLifecycleSupport(mongoDescriptorFacilities);
    }

    @Override
    public Mono<Void> onBeforeConvert(ReactiveBeforeConvertEvent<MongoVersionedModel> event) {
        MongoVersionedModel model = event.getSource();

        return modelsLifecycleSupport.fillRequiredFields(model);
    }

    @Override
    public Mono<Void> onAfterSave(ReactiveAfterSaveEvent<MongoVersionedModel> event) {
        MongoVersionedModel model = event.getSource();

        return modelsLifecycleSupport.createDescriptorIfNeeded(model);
    }

    @Override
    public Mono<Void> onAfterConvert(ReactiveAfterConvertEvent<MongoVersionedModel> event) {
        MongoVersionedModel model = event.getSource();

        return modelsLifecycleSupport.resolveDescriptor(model);
    }

}
