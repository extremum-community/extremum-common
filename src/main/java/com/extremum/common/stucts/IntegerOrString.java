package com.extremum.common.stucts;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class IntegerOrString implements Serializable {
    @JsonProperty("type")
    private Type type;
    @JsonProperty("integerValue")
    private Integer integerValue;
    @JsonProperty("stringValue")
    private String stringValue;

    public IntegerOrString(int value) {
        type = Type.NUMBER;
        integerValue = value;
    }

    public IntegerOrString(String value) {
        type = Type.STRING;
        stringValue = value;
    }

    public boolean isInteger() {
        return Type.NUMBER.equals(type);
    }

    public boolean isString() {
        return Type.STRING.equals(type);
    }

    private enum Type {
        NUMBER, STRING
    }
}
