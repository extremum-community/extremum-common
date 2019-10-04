package io.extremum.common.utils.attribute;

import java.lang.annotation.Annotation;
import java.util.Objects;

public class AdHocAttribute implements Attribute {
    private final Object value;

    public AdHocAttribute(Object value) {
        Objects.requireNonNull(value, "value is null");

        this.value = value;
    }

    @Override
    public String name() {
        throw new IllegalStateException("Ad hoc attribute does not have name");
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        return null;
    }

    @Override
    public Class<?> type() {
        return value.getClass();
    }

    @Override
    public Object value() {
        return value;
    }
}
