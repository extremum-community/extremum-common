package com.extremum.sharedmodels.basic;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.Valid;

/**
 * @author rpuch
 */
@Getter
@Setter
@ToString
public class StringOrObject<T> {
    /**
     * What of fields of the {@link StringOrObject} is initialized
     */
    private Type type;
    private String id;
    @Valid
    private T object;
    
    private StringOrObject(Type type, String id, T object) {
        this.type = type;
        this.id = id;
        this.object = object;
    }

    public StringOrObject(String id) {
        this(Type.simple, id, null);
    }

    public StringOrObject(T object) {
        this(Type.complex, null, object);
    }

    public StringOrObject() {
        this(Type.unknown, null, null);
    }
    
    public boolean isComplex() {
        return type == Type.complex;
    }

    public boolean isSimple() {
        return type == Type.simple;
    }

    public boolean isKnown() {
        return type != Type.unknown;
    }

    public void makeSimple(String id) {
        type = Type.simple;
        this.id = id;
        object = null;
    }

    public void makeComplex(T object) {
        type = Type.complex;
        this.object = object;
        id = null;
    }

    public enum Type {
        unknown,
        /**
         * {@link StringOrObject#id} is initialized, {@link StringOrObject#object} are not
         */
        simple,

        /**
         * {@link StringOrObject#object} is initialized, {@link StringOrObject#id} are not
         */
        complex
    }
}
