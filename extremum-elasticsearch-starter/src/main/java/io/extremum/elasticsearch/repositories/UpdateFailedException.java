package io.extremum.elasticsearch.repositories;

/**
 * @author rpuch
 */
public class UpdateFailedException extends RuntimeException {
    public UpdateFailedException(String message) {
        super(message);
    }
}
