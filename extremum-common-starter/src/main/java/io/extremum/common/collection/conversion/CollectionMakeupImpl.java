package io.extremum.common.collection.conversion;

import io.extremum.common.collection.service.CollectionDescriptorService;
import io.extremum.common.collection.service.ReactiveCollectionDescriptorService;
import io.extremum.common.collection.visit.CollectionVisitDriver;
import io.extremum.common.descriptor.factory.DescriptorSaver;
import io.extremum.common.descriptor.factory.ReactiveDescriptorSaver;
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
    private final DescriptorSaver descriptorSaver;
    private final CollectionDescriptorService collectionDescriptorService;
    private final ReactiveDescriptorSaver reactiveDescriptorSaver;
    private final ReactiveCollectionDescriptorService reactiveCollectionDescriptorService;
    private final CollectionUrls collectionUrls;

    @Override
    public void applyCollectionMakeup(ResponseDto rootDto) {
        List<ReferenceWithContext> collectedReferences = collectReferencesToApplyMakeup(rootDto);

        for (ReferenceWithContext context : collectedReferences) {
            applyMakeupToCollection(context.getReference(), context.getAttribute(), context.getDto());
        }
    }

    @Override
    public Mono<Void> applyCollectionMakeupReactively(ResponseDto rootDto) {
        List<ReferenceWithContext> collectedReferences = collectReferencesToApplyMakeup(rootDto);

        return Flux.fromIterable(collectedReferences)
                .flatMap(context -> applyMakeupToCollectionReactively(
                        context.getReference(), context.getAttribute(), context.getDto()))
                .then();
    }

    private List<ReferenceWithContext> collectReferencesToApplyMakeup(ResponseDto rootDto) {
        List<ReferenceWithContext> collectedReferences = new ArrayList<>();
        CollectionVisitDriver collectionVisitDriver = new CollectionVisitDriver(
                VisitDirection.ROOT_TO_LEAVES,
                (reference, attribute, dto) -> collectReferenceIfEligible(reference, attribute, dto, collectedReferences));
        collectionVisitDriver.visitCollections(rootDto);
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

        applyMakeupWithCollectionDescriptor(reference, collectionDescriptorToUse);
    }

    private void applyMakeupWithCollectionDescriptor(CollectionReference reference, Descriptor collectionDescriptor) {
        reference.setId(collectionDescriptor.getExternalId());

        String externalUrl = collectionUrls.collectionUrl(reference.getId());
        reference.setUrl(externalUrl);
    }

    private Descriptor getExistingOrCreateNewCollectionDescriptor(Attribute attribute, ResponseDto dto) {
        CollectionDescriptor newCollectionDescriptor = collectionDescriptorFor(attribute, dto);

        return collectionDescriptorService.retrieveByCoordinates(newCollectionDescriptor.toCoordinatesString())
                .orElseGet(() -> descriptorSaver.createAndSave(newCollectionDescriptor));
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
                .doOnNext(collectionDescriptor -> applyMakeupWithCollectionDescriptor(reference, collectionDescriptor))
                .then();
    }

    private Mono<Descriptor> getExistingOrCreateNewCollectionDescriptorReactively(Attribute attribute,
                                                                                  ResponseDto dto) {
        CollectionDescriptor newCollectionDescriptor = collectionDescriptorFor(attribute, dto);

        return reactiveCollectionDescriptorService.retrieveByCoordinates(newCollectionDescriptor.toCoordinatesString())
                .switchIfEmpty(Mono.defer(() -> reactiveDescriptorSaver.createAndSave(newCollectionDescriptor)));
    }

    @RequiredArgsConstructor
    @Getter
    private static class ReferenceWithContext {
        private final CollectionReference<?> reference;
        private final ResponseDto dto;
        private final Attribute attribute;
    }
}
