package io.extremum.dynamic.server.exceptions;

import java.io.IOException;

public class SchemaServerException extends RuntimeException {
    public SchemaServerException(String message, IOException cause) {
        super(message, cause);
    }

    public SchemaServerException(String message) {
        super(message);
    }
}
