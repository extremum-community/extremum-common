package io.extremum.common.collection.conversion;

import io.extremum.common.collection.service.CollectionDescriptorService;
import io.extremum.common.collection.service.ReactiveCollectionDescriptorService;
import io.extremum.common.collection.visit.CollectionVisitDriver;
import io.extremum.common.utils.attribute.Attribute;
import io.extremum.common.utils.attribute.VisitDirection;
import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.ResponseDto;
import io.extremum.sharedmodels.fundamental.CollectionReference;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * @author rpuch
 */
@Service
@RequiredArgsConstructor
public class CollectionMakeupImpl implements CollectionMakeup {
    private final CollectionDescriptorService collectionDescriptorService;
    private final ReactiveCollectionDescriptorService reactiveCollectionDescriptorService;
    private final CollectionUrls collectionUrls;

    @Override
    public void applyCollectionMakeup(ResponseDto rootDto) {
        List<ReferenceWithContext> collectedReferences = collectReferencesOnResponseDtoToApplyMakeup(rootDto);

        for (ReferenceWithContext context : collectedReferences) {
            applyMakeupToCollection(context.getReference(), context.getAttribute(), context.getDto());
        }
    }

    @Override
    public Mono<Void> applyCollectionMakeupReactively(ResponseDto rootDto) {
        List<ReferenceWithContext> collectedReferences = collectReferencesOnResponseDtoToApplyMakeup(rootDto);

        return applyReactivelyToCollectedReferences(collectedReferences);
    }

    private Mono<Void> applyReactivelyToCollectedReferences(List<ReferenceWithContext> collectedReferences) {
        return Flux.fromIterable(collectedReferences)
                .flatMap(context -> applyMakeupToCollectionReactively(
                        context.getReference(), context.getAttribute(), context.getDto()))
                .then();
    }

    private List<ReferenceWithContext> collectReferencesOnResponseDtoToApplyMakeup(ResponseDto rootDto) {
        List<ReferenceWithContext> collectedReferences = new ArrayList<>();
        CollectionVisitDriver collectionVisitDriver = createDriver(collectedReferences);
        collectionVisitDriver.visitCollectionsInResponseDto(rootDto);
        return collectedReferences;
    }

    private CollectionVisitDriver createDriver(List<ReferenceWithContext> collectedReferences) {
        return new CollectionVisitDriver(
                    VisitDirection.ROOT_TO_LEAVES,
                    (reference, attribute, dto) -> collectReferenceIfEligible(reference, attribute,
                            dto, collectedReferences));
    }

    @Override
    public Mono<Void> applyCollectionMakeupReactively(CollectionReference<?> reference,
                                                      CollectionDescriptor collectionDescriptor) {
        return reactiveCollectionDescriptorService.retrieveByCoordinatesOrCreate(collectionDescriptor)
                .doOnNext(descriptor -> {
                    reference.setId(descriptor.getExternalId());
                    fillCollectionUrl(reference, descriptor);
                })
                .then(Mono.defer(() -> {
                    List<ReferenceWithContext> collectedReferences = collectReferencesOnNonResponseDtoToApplyMakeup(
                            reference);
                    return applyReactivelyToCollectedReferences(collectedReferences);
                }));
    }

    private List<ReferenceWithContext> collectReferencesOnNonResponseDtoToApplyMakeup(Object root) {
        List<ReferenceWithContext> collectedReferences = new ArrayList<>();
        CollectionVisitDriver collectionVisitDriver = createDriver(collectedReferences);
        collectionVisitDriver.visitCollectionsInNonResponseDto(root);
        return collectedReferences;
    }

    private void collectReferenceIfEligible(CollectionReference reference, Attribute attribute, ResponseDto dto,
                                   List<ReferenceWithContext> collectedReferences) {
        if (dto.getId() == null) {
            return;
        }
        if (!attribute.isAnnotatedWith(OwnedCollection.class)) {
            return;
        }

        collectedReferences.add(new ReferenceWithContext(reference, dto, attribute));
    }

    private void applyMakeupToCollection(CollectionReference reference, Attribute attribute, ResponseDto dto) {
        Descriptor collectionDescriptorToUse = getExistingOrCreateNewCollectionDescriptor(attribute, dto);

        fillCollectionIdAndUrlIfAttributeAllowsIt(reference, collectionDescriptorToUse, attribute);
    }

    private void fillCollectionIdAndUrlIfAttributeAllowsIt(CollectionReference reference,
            Descriptor collectionDescriptor, Attribute attribute) {
        if (shouldFillCollectionId(attribute)) {
            reference.setId(collectionDescriptor.getExternalId());
        }

        fillCollectionUrl(reference, collectionDescriptor);
    }

    private void fillCollectionUrl(CollectionReference reference,
                                   Descriptor collectionDescriptor) {
        String collectionExternalId = collectionDescriptor.getExternalId();

        String externalUrl = collectionUrls.collectionUrl(collectionExternalId);
        reference.setUrl(externalUrl);
    }

    private boolean shouldFillCollectionId(Attribute attribute) {
        FillCollectionId annotation = attribute.getAnnotation(FillCollectionId.class);
        return annotation == null || annotation.value();
    }

    private Descriptor getExistingOrCreateNewCollectionDescriptor(Attribute attribute, ResponseDto dto) {
        CollectionDescriptor newCollectionDescriptor = collectionDescriptorFor(attribute, dto);

        return collectionDescriptorService.retrieveByCoordinatesOrCreate(newCollectionDescriptor);
    }

    private CollectionDescriptor collectionDescriptorFor(Attribute attribute, ResponseDto dto) {
        return CollectionDescriptor.forOwned(
                dto.getId(), getHostAttributeName(attribute));
    }

    private String getHostAttributeName(Attribute attribute) {
        OwnedCollection annotation = attribute.getAnnotation(OwnedCollection.class);
        if (StringUtils.isNotBlank(annotation.hostAttributeName())) {
            return annotation.hostAttributeName();
        }
        return attribute.name();
    }

    private Mono<Void> applyMakeupToCollectionReactively(CollectionReference reference, Attribute attribute,
                                                         ResponseDto dto) {
        return getExistingOrCreateNewCollectionDescriptorReactively(attribute, dto)
                .doOnNext(collectionDescriptor -> fillCollectionIdAndUrlIfAttributeAllowsIt(
                        reference, collectionDescriptor, attribute))
                .then();
    }

    private Mono<Descriptor> getExistingOrCreateNewCollectionDescriptorReactively(Attribute attribute,
                                                                                  ResponseDto dto) {
        CollectionDescriptor newCollectionDescriptor = collectionDescriptorFor(attribute, dto);

        return reactiveCollectionDescriptorService.retrieveByCoordinatesOrCreate(newCollectionDescriptor);
    }

    @RequiredArgsConstructor
    @Getter
    private static class ReferenceWithContext {
        private final CollectionReference<?> reference;
        private final ResponseDto dto;
        private final Attribute attribute;
    }
}
