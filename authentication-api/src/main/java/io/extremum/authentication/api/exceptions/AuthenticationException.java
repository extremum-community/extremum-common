package io.extremum.authentication.api.exceptions;

/**
 * @author rpuch
 */
public class AuthenticationException extends RuntimeException {
    public AuthenticationException(String message) {
        super(message);
    }
}
