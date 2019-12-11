package io.extremum.dynamic.resources;

import io.extremum.dynamic.resources.exceptions.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;

@Slf4j
public class LocalResourceLoader implements ResourceLoader {
    @Override
    public InputStream loadAsInputStream(Path path) throws ResourceNotFoundException {
        try {
            return new FileInputStream(path.toFile());
        } catch (FileNotFoundException e) {
            log.error("File {} not found", path, e);
            throw new ResourceNotFoundException(path, e);
        }
    }
}
