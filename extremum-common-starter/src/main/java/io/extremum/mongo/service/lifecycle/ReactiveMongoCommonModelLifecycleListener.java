package io.extremum.mongo.service.lifecycle;

import io.extremum.mongo.facilities.ReactiveMongoDescriptorFacilities;
import io.extremum.mongo.model.MongoCommonModel;
import io.extremum.mongo.springdata.ReactiveAfterConvertEvent;
import io.extremum.mongo.springdata.ReactiveAfterSaveEvent;
import io.extremum.mongo.springdata.ReactiveBeforeConvertEvent;
import io.extremum.mongo.springdata.lifecycle.AbstractReactiveMongoEventListener;
import reactor.core.publisher.Mono;

/**
 * @author rpuch
 */
public final class ReactiveMongoCommonModelLifecycleListener
        extends AbstractReactiveMongoEventListener<MongoCommonModel> {
    private final ReactiveMongoModelsLifecycleSupport modelsLifecycleSupport;

    public ReactiveMongoCommonModelLifecycleListener(ReactiveMongoDescriptorFacilities mongoDescriptorFacilities) {
        modelsLifecycleSupport = new ReactiveMongoModelsLifecycleSupport(mongoDescriptorFacilities);
    }

    @Override
    public Mono<Void> onBeforeConvert(ReactiveBeforeConvertEvent<MongoCommonModel> event) {
        MongoCommonModel model = event.getSource();

        return modelsLifecycleSupport.fillRequiredFields(model);
    }

    @Override
    public Mono<Void> onAfterSave(ReactiveAfterSaveEvent<MongoCommonModel> event) {
        MongoCommonModel model = event.getSource();

        return modelsLifecycleSupport.createDescriptorIfNeeded(model);
    }

    @Override
    public Mono<Void> onAfterConvert(ReactiveAfterConvertEvent<MongoCommonModel> event) {
        MongoCommonModel model = event.getSource();

        return modelsLifecycleSupport.resolveDescriptor(model);
    }
}
