package com.extremum.common.collection.conversion;

import com.extremum.common.collection.CollectionDescriptor;
import com.extremum.sharedmodels.fundamental.CollectionReference;
import com.extremum.common.collection.service.CollectionDescriptorService;
import com.extremum.sharedmodels.descriptor.Descriptor;
import com.extremum.sharedmodels.dto.ResponseDto;
import com.extremum.common.urls.ApplicationUrls;
import com.extremum.common.utils.attribute.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author rpuch
 */
@Service
public class CollectionMakeupImpl implements CollectionMakeup {
    private static final String COLLECTION_URI_FORMAT = "/collection/%s";
    
    private final CollectionDescriptorService collectionDescriptorService;
    private final ApplicationUrls applicationUrls;
    private final AttributeGraphWalker deepWalker = new DeepAttributeGraphWalker(10,
            CollectionMakeupImpl::shouldGoDeeper);
    private final AttributeGraphWalker shallowWalker = new ShallowAttributeGraphWalker();

    private static boolean shouldGoDeeper(Object object) {
        return object != null && (!(object instanceof Descriptor));
    }

    public CollectionMakeupImpl(CollectionDescriptorService collectionDescriptorService,
            ApplicationUrls applicationUrls) {
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

        CollectionDescriptor collectionDescriptorToUse = getExistingOrCreateNewCollectionDescriptor(attribute, dto);
        reference.setId(collectionDescriptorToUse.getExternalId());

        String collectionUri = String.format(COLLECTION_URI_FORMAT, reference.getId());
        String externalUrl = applicationUrls.createExternalUrl(collectionUri);
        reference.setUrl(externalUrl);
    }

    private CollectionDescriptor getExistingOrCreateNewCollectionDescriptor(Attribute attribute, ResponseDto dto) {
        CollectionDescriptor newDescriptor = CollectionDescriptor.forOwned(dto.getId(), getHostAttributeName(attribute));
        Optional<CollectionDescriptor> existingDescriptor = collectionDescriptorService.retrieveByCoordinates(
                newDescriptor.toCoordinatesString());

        CollectionDescriptor collectionDescriptorToUse;
        if (existingDescriptor.isPresent()) {
            collectionDescriptorToUse = existingDescriptor.get();
        } else {
            collectionDescriptorService.store(newDescriptor);
            collectionDescriptorToUse = newDescriptor;
        }
        return collectionDescriptorToUse;
    }

    private String getHostAttributeName(Attribute attribute) {
        OwnedCollection annotation = attribute.getAnnotation(OwnedCollection.class);
        if (StringUtils.isNotBlank(annotation.hostAttributeName())) {
            return annotation.hostAttributeName();
        }
        return attribute.name();
    }

    private class IsResponseDto implements AttributeVisitor {
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

    private class EligibleForMakeup implements AttributeVisitor {
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
