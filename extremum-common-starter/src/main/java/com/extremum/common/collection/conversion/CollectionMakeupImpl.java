package com.extremum.common.collection.conversion;

import com.extremum.common.collection.CollectionDescriptor;
import com.extremum.common.collection.CollectionReference;
import com.extremum.common.collection.service.CollectionDescriptorService;
import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.dto.ResponseDto;
import com.extremum.common.urls.ApplicationUrls;
import com.extremum.common.utils.attribute.Attribute;
import com.extremum.common.utils.attribute.AttributeGraphWalker;
import com.extremum.common.utils.attribute.AttributeVisitor;
import com.extremum.common.utils.attribute.DeepGraphWalkerWithPropertiesOnlyOnTopLevel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author rpuch
 */
@Service
public class CollectionMakeupImpl implements CollectionMakeup {
    private final CollectionDescriptorService collectionDescriptorService;
    private final ApplicationUrls applicationUrls;
    private final AttributeGraphWalker attributeGraphWalker = new DeepGraphWalkerWithPropertiesOnlyOnTopLevel(5,
            CollectionMakeupImpl::shouldGoDeeper);

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

        AttributeVisitor visitor = attribute -> applyMakeupToField(attribute, dto);
        attributeGraphWalker.walk(dto, new EligibleForMakeup(visitor));
    }

    private void applyMakeupToField(Attribute attribute, ResponseDto dto) {
        if (attribute.value() == null) {
            return;
        }

        CollectionReference reference = (CollectionReference) attribute.value();

        CollectionDescriptor newDescriptor = CollectionDescriptor.forOwned(dto.getId(), getHostAttributeName(attribute));
        Optional<CollectionDescriptor> existingDescriptor = collectionDescriptorService.retrieveByCoordinates(
                newDescriptor.toCoordinatesString());

        if (existingDescriptor.isPresent()) {
            reference.setId(existingDescriptor.get());
        } else {
            collectionDescriptorService.store(newDescriptor);
            reference.setId(newDescriptor);
        }

        String externalUrl = applicationUrls.createExternalUrl("/collection/" + reference.getId());
        reference.setUrl(externalUrl);
    }

    private String getHostAttributeName(Attribute attribute) {
        OwnedCollection annotation = attribute.getAnnotation(OwnedCollection.class);
        if (StringUtils.isNotBlank(annotation.hostAttributeName())) {
            return annotation.hostAttributeName();
        }
        return attribute.name();
    }

    private boolean isOfTypeCollectionReference(Attribute attribute) {
        return CollectionReference.class.isAssignableFrom(attribute.type());
    }

    private boolean isAnnotatedWithOwnedCollection(Attribute attribute) {
        return attribute.isAnnotatedWith(OwnedCollection.class);
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
    }
}
