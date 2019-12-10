package io.extremum.dynamic.services;

import io.extremum.dynamic.SchemaPointer;
import io.extremum.dynamic.models.DynamicModel;
import reactor.core.publisher.Mono;

public interface DynamicModelService<Model extends DynamicModel<?>, Pointer extends SchemaPointer<?>> {
    Mono<Model> saveModel(Pointer bucketId, Model model);
}
