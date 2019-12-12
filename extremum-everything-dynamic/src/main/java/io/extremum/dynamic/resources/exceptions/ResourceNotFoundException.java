package io.extremum.dynamic.resources.exceptions;

import lombok.Getter;

import java.net.URI;

public class ResourceNotFoundException extends Exception {
    @Getter
    private URI resourceUri;

    public ResourceNotFoundException(URI resourceUri) {
        super("Resource wasn't found " + resourceUri.toString());
        this.resourceUri = resourceUri;
    }

    public ResourceNotFoundException(URI resourceUri, Throwable cause) {
        super("Resource wasn't found " + resourceUri.toString(), cause);
        this.resourceUri = resourceUri;
    }
}
