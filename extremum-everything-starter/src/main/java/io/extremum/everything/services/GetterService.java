package io.extremum.everything.services;

import io.extremum.common.models.Model;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

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
        // per https://projectreactor.io/docs/core/release/reference/#faq.wrap-blocking
        return Mono.fromCallable(() -> get(id)).subscribeOn(Schedulers.elastic());
    }
}
