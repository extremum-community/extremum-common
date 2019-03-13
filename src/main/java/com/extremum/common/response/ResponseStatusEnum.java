package com.extremum.common.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;

public enum ResponseStatusEnum {
    /**
     * Everything is done right. No errors and warnings.
     */
    OK("ok"),

    /**
     * The request has been received and now is being processed. Some additional time and/or steps needed to give a response.
     */
    DOING("doing"),

    /**
     * The request has been processed, but there are some warnings in the alerts collection.
     */
    WARNING("warning"),

    /**
     * The request failed because of error(s) listed in the alerts collection.
     */
    FAIL("fail");

    private String value;

    ResponseStatusEnum (String value) {
        this.value = value;
    }

    @JsonGetter
    public String getValue() {
        return this.value;
    }

    @JsonCreator
    public static ResponseStatusEnum fromString(String value) {
        if (value != null) {
            for (ResponseStatusEnum object : ResponseStatusEnum.values()) {
                if (value.equalsIgnoreCase(object.value)) {
                    return object;
                }
            }
        }

        return null;
    }
}
