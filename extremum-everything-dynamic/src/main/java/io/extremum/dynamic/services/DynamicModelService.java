package io.extremum.dynamic.services;

import io.extremum.dynamic.models.DynamicModel;
import reactor.core.publisher.Mono;

public interface DynamicModelService<Model extends DynamicModel<?>> {
    Mono<Model> saveModel(Model model);
}
