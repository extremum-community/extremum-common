package io.extremum.dynamic.dao;

import io.extremum.dynamic.SchemaPointer;
import io.extremum.dynamic.models.DynamicModel;
import reactor.core.publisher.Mono;

public interface DynamicModelDao<Model extends DynamicModel<?>, Pointer extends SchemaPointer<?>> {
    Mono<Model> save(Pointer pointer, Model model);
}
