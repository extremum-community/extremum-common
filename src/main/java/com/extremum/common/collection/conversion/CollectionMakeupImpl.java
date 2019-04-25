package com.extremum.common.collection.conversion;

import com.extremum.common.collection.CollectionDescriptor;
import com.extremum.common.collection.CollectionReference;
import com.extremum.common.collection.service.CollectionDescriptorService;
import com.extremum.common.dto.ResponseDto;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.Optional;

/**
 * @author rpuch
 */
@Service
public class CollectionMakeupImpl implements CollectionMakeup {
    private final CollectionDescriptorService collectionDescriptorService;

    public CollectionMakeupImpl(CollectionDescriptorService collectionDescriptorService) {
        this.collectionDescriptorService = collectionDescriptorService;
    }

    @Override
    public void applyCollectionMakeup(ResponseDto dto) {
        if (dto.getId() == null) {
            return;
        }

        new InstanceFields(dto.getClass()).stream()
                .filter(this::isOfTypeCollectionReference)
                .filter(this::isAnnotatedWithEmbeddedCollection)
                .forEach(field -> applyMakeupToField(field, dto));
    }

    private void applyMakeupToField(Field field, ResponseDto dto) {
        Object value = getFieldValue(dto, field);
        if (value == null) {
            return;
        }

        CollectionReference reference = (CollectionReference) value;

        CollectionDescriptor newDescriptor = CollectionDescriptor.forEmbedded(dto.getId(), getHostFieldName(field));
        Optional<CollectionDescriptor> existingDescriptor = collectionDescriptorService.retrieveByCoordinates(
                newDescriptor.toCoordinatesString());

        if (existingDescriptor.isPresent()) {
            reference.setDescriptor(existingDescriptor.get());
        } else {
            collectionDescriptorService.store(newDescriptor);
            reference.setDescriptor(newDescriptor);
        }
    }

    private String getHostFieldName(Field field) {
        MongoEmbeddedCollection annotation = field.getAnnotation(MongoEmbeddedCollection.class);
        if (StringUtils.isNotBlank(annotation.hostFieldName())) {
            return annotation.hostFieldName();
        }
        return field.getName();
    }

    private boolean isOfTypeCollectionReference(Field field) {
        return field.getType() == CollectionReference.class;
    }

    private boolean isAnnotatedWithEmbeddedCollection(Field field) {
        return field.getAnnotation(MongoEmbeddedCollection.class) != null;
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
}
