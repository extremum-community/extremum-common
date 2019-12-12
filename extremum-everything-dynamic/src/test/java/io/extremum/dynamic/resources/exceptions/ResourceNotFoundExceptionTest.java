package io.extremum.dynamic.resources.exceptions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

class ResourceNotFoundExceptionTest {
    @Test
    void getResourcePath() {
        Path path = Paths.get("path");
        ResourceNotFoundException ex = new ResourceNotFoundException(path);

        Assertions.assertEquals(path.toString(), ex.getResourcePath().toString());
    }

    @Test
    void messageContainsAPath() {
        Path path = Paths.get("path");
        ResourceNotFoundException ex = new ResourceNotFoundException(path);

        Assertions.assertEquals("Resource wasn't found " + path, ex.getMessage());
    }
}