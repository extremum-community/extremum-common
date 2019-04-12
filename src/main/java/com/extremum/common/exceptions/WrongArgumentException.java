package com.extremum.common.exceptions;

import org.springframework.http.HttpStatus;

public class WrongArgumentException extends CommonException {

    public WrongArgumentException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
