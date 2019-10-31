package io.extremum.common.pool;

public class FactoryClosedException extends RuntimeException {
    public FactoryClosedException(String message) {
        super(message);
    }
}
