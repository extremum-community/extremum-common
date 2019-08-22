package io.extremum.everything.services.management;

import io.extremum.common.models.Model;
import reactor.core.publisher.Mono;

/**
 * @author rpuch
 */
public interface Getter {
    Model get(String id);

    Mono<Model> reactiveGet(String id);
}
