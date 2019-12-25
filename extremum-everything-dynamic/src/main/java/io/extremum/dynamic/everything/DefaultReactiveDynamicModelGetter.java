package io.extremum.dynamic.everything;

import io.extremum.common.exceptions.ModelNotFoundException;
import io.extremum.dynamic.services.impl.JsonBasedDynamicModelService;
import io.extremum.everything.services.management.ReactiveDynamicModelGetter;
import io.extremum.mongo.facilities.ReactiveMongoDescriptorFacilities;
import io.extremum.sharedmodels.basic.Model;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DefaultReactiveDynamicModelGetter implements ReactiveDynamicModelGetter {
    private final JsonBasedDynamicModelService dynamicModelService;
    private final ReactiveMongoDescriptorFacilities reactiveMongoDescriptorFacilities;

    @Override
    public Mono<Model> get(String id) {
        Objects.requireNonNull(id, "ID can;t be null");

        return reactiveMongoDescriptorFacilities.fromInternalId(new ObjectId(id))
                .flatMap(dynamicModelService::findById)
                .onErrorResume(ModelNotFoundException.class, e -> Mono.empty())
                .cast(Model.class);
    }
}
