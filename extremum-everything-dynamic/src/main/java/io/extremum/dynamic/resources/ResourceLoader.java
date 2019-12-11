package io.extremum.dynamic.resources;

import io.extremum.dynamic.resources.exceptions.ResourceNotFoundException;

import java.io.InputStream;
import java.nio.file.Path;

public interface ResourceLoader {
    InputStream loadAsInputStream(Path path) throws ResourceNotFoundException;
}
