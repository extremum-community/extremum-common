package io.extremum.everything.services.management;

import io.extremum.common.models.Model;
import reactor.core.publisher.Mono;

/**
 * @author rpuch
 */
public interface ReactiveGetter {
    Mono<Model> reactiveGet(String id);
}
