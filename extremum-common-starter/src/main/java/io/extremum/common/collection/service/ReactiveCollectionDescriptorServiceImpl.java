package io.extremum.common.collection.service;

import com.google.common.collect.ImmutableList;
import io.extremum.common.descriptor.dao.ReactiveDescriptorDao;
import io.extremum.common.descriptor.factory.DescriptorSavers;
import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;
import org.springframework.dao.DuplicateKeyException;
import reactor.core.publisher.Mono;

import java.util.List;

public class ReactiveCollectionDescriptorServiceImpl implements ReactiveCollectionDescriptorService {
    private final ReactiveDescriptorDao reactiveDescriptorDao;
    private final DescriptorSavers descriptorSavers;
    private final List<ReactiveCollectionDescriptorExtractionOverride> extractionOverrides;

    private final CollectionDescriptorVerifier collectionDescriptorVerifier = new CollectionDescriptorVerifier();

    public ReactiveCollectionDescriptorServiceImpl(ReactiveDescriptorDao reactiveDescriptorDao,
            DescriptorService descriptorService,
            List<ReactiveCollectionDescriptorExtractionOverride> extractionOverrides) {
        this.reactiveDescriptorDao = reactiveDescriptorDao;
        this.descriptorSavers = new DescriptorSavers(descriptorService);
        this.extractionOverrides = ImmutableList.copyOf(extractionOverrides);
    }

    @Override
    public Mono<CollectionDescriptor> retrieveByExternalId(String externalId) {
        return reactiveDescriptorDao.retrieveByExternalId(externalId)
                .flatMap(descriptor -> {
                    return extractCollection(externalId, descriptor);
                });
    }

    private Mono<? extends CollectionDescriptor> extractCollection(String externalId, Descriptor descriptor) {
        for (ReactiveCollectionDescriptorExtractionOverride override : extractionOverrides) {
            if (override.supports(descriptor)) {
                return override.extractCollectionFromDescriptor(descriptor);
            }
        }
        return Mono.fromSupplier(() -> extractCollectionInTheStandardWay(externalId, descriptor));
    }

    private CollectionDescriptor extractCollectionInTheStandardWay(String externalId, Descriptor descriptor) {
        collectionDescriptorVerifier.makeSureDescriptorContainsCollection(externalId, descriptor);
        return descriptor.getCollection();
    }

    @Override
    public Mono<Descriptor> retrieveByCoordinatesOrCreate(CollectionDescriptor collectionDescriptor) {
        Descriptor descriptor = descriptorSavers.createCollectionDescriptor(collectionDescriptor);
        return reactiveDescriptorDao.store(descriptor)
                .onErrorResume(DuplicateKeyException.class,
                        e -> retrieveByCoordinates(collectionDescriptor.toCoordinatesString()));
    }

    private Mono<Descriptor> retrieveByCoordinates(String coordinatesString) {
        return reactiveDescriptorDao.retrieveByCollectionCoordinates(coordinatesString)
                .map(descriptor -> {
                    collectionDescriptorVerifier.makeSureDescriptorContainsCollection(
                            descriptor.getExternalId(), descriptor);
                    return descriptor;
                });
    }
}
