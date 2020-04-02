package io.extremum.mongo.service.lifecycle;

import io.extremum.common.facilities.ReactiveDescriptorFacilities;
import io.extremum.common.model.HasUuid;
import io.extremum.mongo.springdata.ReactiveAfterConvertEvent;
import io.extremum.mongo.springdata.ReactiveAfterSaveEvent;
import io.extremum.mongo.springdata.ReactiveBeforeConvertEvent;
import io.extremum.mongo.springdata.lifecycle.AbstractReactiveMongoEventListener;
import reactor.core.publisher.Mono;

/**
 * @author rpuch
 */
public abstract class ReactiveMongoLifecycleListener<T extends HasUuid>
        extends AbstractReactiveMongoEventListener<T> {
    private final ReactiveMongoModelsLifecycleSupport<T> modelsLifecycleSupport;

    public ReactiveMongoLifecycleListener(ReactiveDescriptorFacilities descriptorFacilities,
                                          InternalIdAdapter<? super T> adapter) {
        modelsLifecycleSupport = new ReactiveMongoModelsLifecycleSupport<>(descriptorFacilities, adapter);
    }

    @Override
    public Mono<Void> onBeforeConvert(ReactiveBeforeConvertEvent<T> event) {
        T model = event.getSource();

        return modelsLifecycleSupport.fillRequiredFields(model);
    }

    @Override
    public Mono<Void> onAfterSave(ReactiveAfterSaveEvent<T> event) {
        T model = event.getSource();

        return modelsLifecycleSupport.createDescriptorIfNeeded(model);
    }

    @Override
    public Mono<Void> onAfterConvert(ReactiveAfterConvertEvent<T> event) {
        T model = event.getSource();

        return modelsLifecycleSupport.fillDescriptorFromInternalId(model);
    }
}
