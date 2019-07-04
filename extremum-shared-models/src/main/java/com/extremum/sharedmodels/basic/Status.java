package com.extremum.sharedmodels.basic;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Status {
    draft("draft"), active("active"), hidden("hidden");

    private final String value;

    Status(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static Status fromString(String value) {
        if (value != null) {
            for (Status type : Status.values()) {
                if (value.equalsIgnoreCase(type.value)) {
                    return type;
                }
            }
        }

        return null;
    }
}
