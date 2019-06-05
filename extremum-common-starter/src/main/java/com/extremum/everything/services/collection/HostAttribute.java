package com.extremum.everything.services.collection;

import com.extremum.common.models.Model;
import com.extremum.common.utils.ModelUtils;
import com.extremum.everything.collection.CollectionElementType;
import com.extremum.everything.exceptions.EverythingEverythingException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * @author rpuch
 */
class HostAttribute {
    private final String name;
    private final Method getter;
    private final Field field;

    HostAttribute(String name, Method getter, Field field) {
        this.name = name;
        this.getter = getter;
        this.field = field;
    }

    public String name() {
        return name;
    }

    public Method getter() {
        return getter;
    }

    public Optional<Field> field() {
        return Optional.ofNullable(field);
    }

    Class<? extends Model> detectElementClass(Model host) {
        CollectionElementType fieldAnnotation = field()
                .map(field -> field.getAnnotation(CollectionElementType.class))
                .orElse(null);
        CollectionElementType getterAnnotation = getter().getAnnotation(CollectionElementType.class);
        if (fieldAnnotation == null && getterAnnotation == null) {
            String name = ModelUtils.getModelName(host);
            String message = String.format(
                    "For host type '%s' attribute '%s' does not contain @CollectionElementType annotation",
                    name, name());
            throw new EverythingEverythingException(message);
        }

        if (getterAnnotation != null) {
            return getterAnnotation.value();
        }
        return fieldAnnotation.value();
    }
}
