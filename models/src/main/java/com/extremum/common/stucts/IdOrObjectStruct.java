package com.extremum.common.stucts;


import lombok.Getter;

import javax.validation.Valid;
import java.io.Serializable;
import java.util.function.Function;

@Getter
public class IdOrObjectStruct<ID extends Serializable, T> {
    public IdOrObjectStruct(Type type, ID id, T object) {
        this.type = type;
        this.id = id;
        this.object = object;
    }

    public IdOrObjectStruct(ID id) {
        this(Type.simple, id, null);
    }

    public IdOrObjectStruct(T object) {
        this(Type.complex, null, object);
    }

    public IdOrObjectStruct() {
        this(Type.unknown, null, null);
    }

    /**
     * Who of fields of the {@link IdOrObjectStruct} is initialized
     */
    public Type type;
    public ID id;
    @Valid
    public T object;

    public boolean isComplex() {
        return type == Type.complex;
    }

    public boolean isSimple() {
        return type == Type.simple;
    }

    public boolean isKnown() {
        return type != Type.unknown;
    }

    public void makeSimple(ID id) {
        type = Type.simple;
        this.id = id;
        object = null;
    }

    public void makeComplex(T object) {
        type = Type.complex;
        this.object = object;
        id = null;
    }

    public String getInternalIdOrNull(Function<? super ID, String> idConverter) {
        if (isSimple()) {
            return idConverter.apply(id);
        }

        return null;
    }

    public enum Type {
        unknown,
        /**
         * {@link IdOrObjectStruct#id} is initialized, {@link IdOrObjectStruct#object} are not
         */
        simple,

        /**
         * {@link IdOrObjectStruct#object} is initialized, {@link IdOrObjectStruct#id} are not
         */
        complex
    }
}
