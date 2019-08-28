package io.extremum.common.collection.conversion;

import io.extremum.common.collection.service.CollectionDescriptorService;
import io.extremum.common.descriptor.factory.DescriptorSaver;
import io.extremum.common.urls.ApplicationUrls;
import io.extremum.common.utils.attribute.*;
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
    private static final String COLLECTION_URI_FORMAT = "/collection/%s";

    private final DescriptorSaver descriptorSaver;
    private final CollectionDescriptorService collectionDescriptorService;
    private final ApplicationUrls applicationUrls;
    private final AttributeGraphWalker deepWalker = new DeepAttributeGraphWalker(10,
            CollectionMakeupImpl::shouldGoDeeper);
    private final AttributeGraphWalker shallowWalker = new ShallowAttributeGraphWalker();

    private static boolean shouldGoDeeper(Object object) {
        return object != null && (!(object instanceof Descriptor));
    }

    public CollectionMakeupImpl(DescriptorSaver descriptorSaver,
                                CollectionDescriptorService collectionDescriptorService,
                                ApplicationUrls applicationUrls) {
        this.descriptorSaver = descriptorSaver;
        this.collectionDescriptorService = collectionDescriptorService;
        this.applicationUrls = applicationUrls;
    }

    @Override
    public void applyCollectionMakeup(ResponseDto dto) {
        if (dto.getId() == null) {
            return;
        }

        applyMakeupToResponseDtoWithoutRecursion(dto);

        AttributeVisitor dtoVisitor = this::applyMakeupToResponseDtoInAttribute;
        deepWalker.walk(dto, new IsResponseDto(dtoVisitor));
    }

    private void applyMakeupToResponseDtoInAttribute(Attribute dtoAttribute) {
        ResponseDto dto = (ResponseDto) dtoAttribute.value();
        if (dto == null) {
            return;
        }

        applyMakeupToResponseDtoWithoutRecursion(dto);
    }

    private void applyMakeupToResponseDtoWithoutRecursion(ResponseDto dto) {
        AttributeVisitor visitor = attribute -> applyMakeupToAttribute(attribute, dto);
        shallowWalker.walk(dto, new EligibleForMakeup(visitor));
    }

    private void applyMakeupToAttribute(Attribute attribute, ResponseDto dto) {
        if (attribute.value() == null) {
            return;
        }

        CollectionReference reference = (CollectionReference) attribute.value();

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

    private static class IsResponseDto implements AttributeVisitor {
        private final AttributeVisitor visitor;

        private IsResponseDto(AttributeVisitor visitor) {
            this.visitor = visitor;
        }

        @Override
        public void visitAttribute(Attribute attribute) {
            Object value = attribute.value();
            if (value != null && ResponseDto.class.isAssignableFrom(value.getClass())) {
                visitor.visitAttribute(attribute);
            }
        }
    }

    private static class EligibleForMakeup implements AttributeVisitor {
        private final AttributeVisitor visitor;

        private EligibleForMakeup(AttributeVisitor visitor) {
            this.visitor = visitor;
        }

        @Override
        public void visitAttribute(Attribute attribute) {
            if (isOfTypeCollectionReference(attribute) && isAnnotatedWithOwnedCollection(attribute)) {
                visitor.visitAttribute(attribute);
            }
        }

        private boolean isOfTypeCollectionReference(Attribute attribute) {
            return CollectionReference.class.isAssignableFrom(attribute.type());
        }

        private boolean isAnnotatedWithOwnedCollection(Attribute attribute) {
            return attribute.isAnnotatedWith(OwnedCollection.class);
        }
    }
}
