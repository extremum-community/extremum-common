package io.extremum.mongo.facilities;

import io.extremum.sharedmodels.descriptor.Descriptor;
import org.bson.types.ObjectId;
import reactor.core.publisher.Mono;

/**
 * @author rpuch
 */
public interface ReactiveMongoDescriptorFacilities {
    Mono<Descriptor> create(ObjectId id, String modelType);

    Mono<Descriptor> fromInternalId(ObjectId internalId);

    Mono<ObjectId> resolve(Descriptor descriptor);

    Mono<Descriptor> makeDescriptorReady(String descriptorExternalId, String modelType);
}
