package io.extremum.dynamic.resources.exceptions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URI;

class ResourceNotFoundExceptionTest {
    @Test
    void getResourcePath() {
        URI uri = URI.create("path");
        ResourceNotFoundException ex = new ResourceNotFoundException(uri);

        Assertions.assertEquals(uri.toString(), ex.getResourceUri().toString());
    }

    @Test
    void messageContainsAPath() {
        URI uri = URI.create("path");
        ResourceNotFoundException ex = new ResourceNotFoundException(uri);

        Assertions.assertEquals("Resource wasn't found " + uri, ex.getMessage());
    }
}