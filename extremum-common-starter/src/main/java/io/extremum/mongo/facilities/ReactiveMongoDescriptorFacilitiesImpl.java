package io.extremum.mongo.facilities;

import io.extremum.common.descriptor.dao.ReactiveDescriptorDao;
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
    private final ReactiveDescriptorDao reactiveDescriptorDao;

    private final DescriptorReadinessValidation descriptorReadinessValidation = new DescriptorReadinessValidation();

    @Override
    public Mono<Descriptor> create(ObjectId id, String modelType) {
        return descriptorSaver.createAndSave(id.toString(), modelType, STORAGE_TYPE);
    }

    @Override
    public Mono<Descriptor> fromInternalId(ObjectId internalId) {
        return Mono.just(descriptorFactory.fromInternalId(internalId.toString(), STORAGE_TYPE));
    }

    @Override
    public Mono<ObjectId> resolve(Descriptor descriptor) {
        return DescriptorResolver.resolveReactively(descriptor, STORAGE_TYPE)
                .map(ObjectId::new);
    }

    @Override
    public Mono<Descriptor> makeDescriptorReady(String descriptorExternalId, String modelType) {
        return reactiveDescriptorDao.retrieveByExternalId(descriptorExternalId)
                .doOnNext(descriptor -> descriptorReadinessValidation.validateDescriptorIsNotReady(
                        descriptorExternalId, descriptor))
                .doOnNext(descriptor -> {
                    descriptor.setReadiness(Descriptor.Readiness.READY);
                    descriptor.setModelType(modelType);
                })
                .flatMap(reactiveDescriptorDao::store);
    }
}
