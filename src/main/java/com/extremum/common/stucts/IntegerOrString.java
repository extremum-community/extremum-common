package com.extremum.common.stucts;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class IntegerOrString implements Serializable {
    private Type type;
    private Integer integerValue;
    private String stringValue;

    public IntegerOrString(int value) {
        type = Type.number;
        integerValue = value;
    }

    public IntegerOrString(String value) {
        type = Type.string;
        stringValue = value;
    }

    public boolean isInteger() {
        return Type.number.equals(type);
    }

    public boolean isString() {
        return Type.string.equals(type);
    }

    private enum Type {
        number, string
    }
}
