package io.extremum.common.service;

import io.extremum.common.model.Model;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ReactiveCommonService<M extends Model> {
    Mono<M> get(String id);

    Mono<M> delete(String id);

    Flux<M> list();

    Mono<M> save(M data);

    Mono<M> create(M data);

    Flux<M> create(List<M> data);
}
