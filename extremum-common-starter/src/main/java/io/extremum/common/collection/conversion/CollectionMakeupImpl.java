package io.extremum.common.collection.conversion;

import io.extremum.common.collection.service.CollectionDescriptorService;
import io.extremum.common.collection.visit.CollectionVisitDriver;
import io.extremum.common.descriptor.factory.DescriptorSaver;
import io.extremum.common.urls.ApplicationUrls;
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

import java.util.ArrayList;
import java.util.List;

/**
 * @author rpuch
 */
@Service
public class CollectionMakeupImpl implements CollectionMakeup {
    private static final String COLLECTION_URI_FORMAT = "/%s";

    private final DescriptorSaver descriptorSaver;
    private final CollectionDescriptorService collectionDescriptorService;
    private final ApplicationUrls applicationUrls;

    public CollectionMakeupImpl(DescriptorSaver descriptorSaver,
                                CollectionDescriptorService collectionDescriptorService,
                                ApplicationUrls applicationUrls) {
        this.descriptorSaver = descriptorSaver;
        this.collectionDescriptorService = collectionDescriptorService;
        this.applicationUrls = applicationUrls;
    }

    @Override
    public void applyCollectionMakeup(ResponseDto rootDto) {
        List<ReferenceWithContext> collectedReferences = collectReferencesToApplyMakeup(rootDto);

        for (ReferenceWithContext context : collectedReferences) {
            applyMakeupToCollection(context.getReference(), context.getAttribute(), context.getDto());
        }
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

        String collectionUri = String.format(COLLECTION_URI_FORMAT, reference.getId());
        String externalUrl = applicationUrls.createExternalUrl(collectionUri);
        reference.setUrl(externalUrl);
    }

    private Descriptor getExistingOrCreateNewCollectionDescriptor(Attribute attribute, ResponseDto dto) {
        CollectionDescriptor newCollectionDescriptor = CollectionDescriptor.forOwned(
                dto.getId(), getHostAttributeName(attribute));

        return collectionDescriptorService.retrieveByCoordinates(newCollectionDescriptor.toCoordinatesString())
                .orElseGet(() -> descriptorSaver.createAndSave(newCollectionDescriptor));
    }

    private String getHostAttributeName(Attribute attribute) {
        OwnedCollection annotation = attribute.getAnnotation(OwnedCollection.class);
        if (StringUtils.isNotBlank(annotation.hostAttributeName())) {
            return annotation.hostAttributeName();
        }
        return attribute.name();
    }

    @RequiredArgsConstructor
    @Getter
    private static class ReferenceWithContext {
        private final CollectionReference<?> reference;
        private final ResponseDto dto;
        private final Attribute attribute;
    }
}
