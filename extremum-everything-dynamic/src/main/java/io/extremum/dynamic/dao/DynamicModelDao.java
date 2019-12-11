package io.extremum.dynamic.dao;

import io.extremum.dynamic.models.DynamicModel;
import reactor.core.publisher.Mono;

public interface DynamicModelDao<Model extends DynamicModel<?>> {
    Mono<Model> save(Model model);
}
