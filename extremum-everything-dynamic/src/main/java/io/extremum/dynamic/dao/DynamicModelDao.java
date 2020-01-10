package io.extremum.dynamic.dao;

import io.extremum.dynamic.models.DynamicModel;
import io.extremum.sharedmodels.descriptor.Descriptor;
import reactor.core.publisher.Mono;

public interface DynamicModelDao<Model extends DynamicModel<?>> {
    Mono<Model> create(Model model, String collectionName);

    Mono<Model> replace(Model model, String collectionName);

    Mono<Model> getByIdFromCollection(Descriptor id, String collectionName);

    Mono<Void> remove(Descriptor id, String collectionName);
}
