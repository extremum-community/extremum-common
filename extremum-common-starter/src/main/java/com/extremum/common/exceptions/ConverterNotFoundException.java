package com.extremum.common.exceptions;

public class ConverterNotFoundException extends CommonException {
    public ConverterNotFoundException(String message) {
        super(message, 404);
    }
}
