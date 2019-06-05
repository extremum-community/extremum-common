package com.extremum.common.utils.attribute;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author rpuch
 */
class GetFieldValue implements Supplier<Object> {
    private final Field field;
    private final Object target;

    GetFieldValue(Field field, Object target) {
        Objects.requireNonNull(field, "Field cannot be null");
        Objects.requireNonNull(target, "Target cannot be null");

        this.field = field;
        this.target = target;
    }

    @Override
    public Object get() {
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
        try {
            return field.get(target);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot get field value", e);
        }
    }
}
