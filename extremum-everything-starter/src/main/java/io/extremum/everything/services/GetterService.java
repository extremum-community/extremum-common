package io.extremum.everything.services;

import io.extremum.common.models.Model;
import reactor.core.publisher.Mono;

/**
 * Service that is used to obtain a model from the database for Everything-Everything GET operation
 * and PATCH operation.
 *
 * @param <M> model type
 */
public interface GetterService<M extends Model> extends EverythingEverythingService {
    /**
     * Search and returns a found object
     *
     * @param id descriptor internal ID
     * @return found object if its found or null otherwise
     */
    M get(String id);

    default Mono<M> reactiveGet(String id) {
        // TODO: use real reactivity
        return Mono.defer(() -> Mono.justOrEmpty(get(id)));
    }
}
