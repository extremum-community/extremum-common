package com.extremum.elasticsearch.repositories;

import com.extremum.common.utils.InstanceFields;
import com.extremum.elasticsearch.annotation.PrimaryTerm;
import com.extremum.elasticsearch.annotation.SequenceNumber;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Objects;

/**
 * @author rpuch
 */
class SequenceNumbers {
    boolean hasSequenceNumber(Object object) {
        return getOptionalSequenceNumber(object) != null;
    }

    private Long getOptionalSequenceNumber(Object object) {
        return getOptionalLong(object, SequenceNumber.class);
    }

    private Long getOptionalLong(Object object, Class<? extends Annotation> annotationClass) {
        // TODO: optimize this
        return new InstanceFields(object.getClass()).stream()
                .filter(field -> field.getAnnotation(annotationClass) != null)
                .filter(field -> field.getType() == Long.class)
                .map(field -> getFieldValue(field, object))
                .filter(Objects::nonNull)
                .map(Long.class::cast)
                .findFirst()
                .orElse(null);
    }

    private Object getFieldValue(Field field, Object object) {
        field.setAccessible(true);

        try {
            return field.get(object);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot get field value", e);
        }
    }

    long getRequiredSequenceNumber(Object object) {
        Long sequenceNumber = getOptionalSequenceNumber(object);
        Objects.requireNonNull(sequenceNumber);
        return sequenceNumber;
    }

    boolean hasPrimaryTerm(Object object) {
        return getOptionalPrimaryTerm(object) != null;
    }

    private Long getOptionalPrimaryTerm(Object object) {
        return getOptionalLong(object, PrimaryTerm.class);
    }

    long getRequiredPrimaryTerm(Object object) {
        Long primaryTerm = getOptionalPrimaryTerm(object);
        Objects.requireNonNull(primaryTerm);
        return primaryTerm;
    }
}
