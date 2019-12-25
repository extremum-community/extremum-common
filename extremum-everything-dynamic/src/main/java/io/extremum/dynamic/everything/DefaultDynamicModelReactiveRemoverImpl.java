package io.extremum.dynamic.everything;

import io.extremum.dynamic.services.impl.JsonBasedDynamicModelService;
import io.extremum.everything.services.defaultservices.DefaultDynamicModelReactiveRemover;
import io.extremum.mongo.facilities.ReactiveMongoDescriptorFacilities;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DefaultDynamicModelReactiveRemoverImpl implements DefaultDynamicModelReactiveRemover {
    private final JsonBasedDynamicModelService dynamicModelService;
    private final ReactiveMongoDescriptorFacilities reactiveMongoDescriptorFacilities;

    @Override
    public Mono<Void> remove(String id) {
        Objects.requireNonNull(id, "ID can't be null");

        return reactiveMongoDescriptorFacilities.fromInternalId(new ObjectId(id))
                .flatMap(dynamicModelService::remove);
    }
}
