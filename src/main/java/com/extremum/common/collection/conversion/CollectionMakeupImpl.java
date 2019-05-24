package com.extremum.common.collection.conversion;

import com.extremum.common.collection.CollectionDescriptor;
import com.extremum.common.collection.CollectionReference;
import com.extremum.common.collection.service.CollectionDescriptorService;
import com.extremum.common.dto.ResponseDto;
import com.extremum.common.urls.ApplicationUrls;
import com.extremum.common.utils.DeepFieldGraphWalker;
import com.extremum.common.utils.FieldGraphWalker;
import com.extremum.common.utils.FieldVisitor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author rpuch
 */
@Service
public class CollectionMakeupImpl implements CollectionMakeup {
    private final CollectionDescriptorService collectionDescriptorService;
    private final ApplicationUrls applicationUrls;
    private final FieldGraphWalker fieldGraphWalker = new DeepFieldGraphWalker(5);

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

        FieldVisitor visitor = (field, lazyValue) -> applyMakeupToField(field, lazyValue.get(), dto);
        fieldGraphWalker.walk(dto, new EligibleForMakeup(visitor));
    }

    private void applyMakeupToField(Field field, Object value, ResponseDto dto) {
        if (value == null) {
            return;
        }

        CollectionReference reference = (CollectionReference) value;

        CollectionDescriptor newDescriptor = CollectionDescriptor.forOwned(dto.getId(), getHostFieldName(field));
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

    private String getHostFieldName(Field field) {
        OwnedCollection annotation = field.getAnnotation(OwnedCollection.class);
        if (StringUtils.isNotBlank(annotation.hostFieldName())) {
            return annotation.hostFieldName();
        }
        return field.getName();
    }

    private boolean isOfTypeCollectionReference(Field field) {
        return field.getType() == CollectionReference.class;
    }

    private boolean isAnnotatedWithOwnedCollection(Field field) {
        return field.getAnnotation(OwnedCollection.class) != null;
    }

    private Object getFieldValue(ResponseDto dto, Field field) {
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
        try {
            return field.get(dto);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot get field value", e);
        }
    }

    private class EligibleForMakeup implements FieldVisitor {
        private final FieldVisitor visitor;

        private EligibleForMakeup(FieldVisitor visitor) {
            this.visitor = visitor;
        }

        @Override
        public void visitField(Field field, Supplier<Object> lazyValue) {
            if (isOfTypeCollectionReference(field) && isAnnotatedWithOwnedCollection(field)) {
                visitor.visitField(field, lazyValue);
            }
        }
    }
}
