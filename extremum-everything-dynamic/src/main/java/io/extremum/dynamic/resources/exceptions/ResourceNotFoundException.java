package io.extremum.dynamic.resources.exceptions;

import lombok.Getter;

import java.nio.file.Path;

public class ResourceNotFoundException extends Exception {
    @Getter
    private Path resourcePath;

    public ResourceNotFoundException(Path resourcePath) {
        super("Resource wasn't found " + resourcePath.toString());
        this.resourcePath = resourcePath;
    }

    public ResourceNotFoundException(Path resourcePath, Throwable cause) {
        super("Resource wasn't found " + resourcePath.toString(), cause);
        this.resourcePath = resourcePath;
    }
}
