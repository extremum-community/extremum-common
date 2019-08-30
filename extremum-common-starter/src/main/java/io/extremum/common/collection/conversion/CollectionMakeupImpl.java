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
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @author rpuch
 */
@Service
public class CollectionMakeupImpl implements CollectionMakeup {
    private static final String COLLECTION_URI_FORMAT = "/%s";

    private final DescriptorSaver descriptorSaver;
    private final CollectionDescriptorService collectionDescriptorService;
    private final ApplicationUrls applicationUrls;

    private final CollectionVisitDriver collectionVisitDriver = new CollectionVisitDriver(
            VisitDirection.ROOT_TO_LEAVES, this::applyToCollection);

    public CollectionMakeupImpl(DescriptorSaver descriptorSaver,
                                CollectionDescriptorService collectionDescriptorService,
                                ApplicationUrls applicationUrls) {
        this.descriptorSaver = descriptorSaver;
        this.collectionDescriptorService = collectionDescriptorService;
        this.applicationUrls = applicationUrls;
    }

    @Override
    public void applyCollectionMakeup(ResponseDto dto) {
        collectionVisitDriver.visitCollections(dto);
    }

    private void applyToCollection(CollectionReference reference, Attribute attribute, ResponseDto dto) {
        if (!attribute.isAnnotatedWith(OwnedCollection.class)) {
            return;
        }

        Descriptor collectionDescriptorToUse = getExistingOrCreateNewCollectionDescriptor(attribute, dto);
        reference.setId(collectionDescriptorToUse.getExternalId());

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

}
