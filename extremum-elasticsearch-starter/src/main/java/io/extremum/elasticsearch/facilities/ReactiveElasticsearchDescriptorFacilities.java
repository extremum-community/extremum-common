package io.extremum.elasticsearch.facilities;

import io.extremum.sharedmodels.descriptor.Descriptor;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * @author rpuch
 */
public interface ReactiveElasticsearchDescriptorFacilities {
    Mono<Descriptor> create(UUID uuid, String modelType);

    Mono<UUID> resolve(Descriptor descriptor);
}
