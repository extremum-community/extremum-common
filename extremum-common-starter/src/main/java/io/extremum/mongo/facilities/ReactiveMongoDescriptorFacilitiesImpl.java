package io.extremum.mongo.facilities;

import io.extremum.common.descriptor.factory.DescriptorFactory;
import io.extremum.common.descriptor.factory.DescriptorResolver;
import io.extremum.common.descriptor.factory.ReactiveDescriptorSaver;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public final class ReactiveMongoDescriptorFacilitiesImpl implements ReactiveMongoDescriptorFacilities {
    private static final Descriptor.StorageType STORAGE_TYPE = Descriptor.StorageType.MONGO;

    private final DescriptorFactory descriptorFactory;
    private final ReactiveDescriptorSaver descriptorSaver;

    @Override
    public Mono<Descriptor> create(ObjectId id, String modelType) {
        return descriptorSaver.createAndSaveReactively(id.toString(), modelType, STORAGE_TYPE);
    }

    @Override
    public Mono<Descriptor> fromInternalId(ObjectId internalId) {
        return Mono.just(descriptorFactory.fromInternalId(internalId.toString(), STORAGE_TYPE));
    }

    @Override
    public Mono<ObjectId> resolve(Descriptor descriptor) {
        return Mono.defer(() -> {
            String internalId = DescriptorResolver.resolve(descriptor, STORAGE_TYPE);
            return Mono.just(new ObjectId(internalId));
        });
    }
}
