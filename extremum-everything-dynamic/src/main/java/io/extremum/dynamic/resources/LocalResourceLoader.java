package io.extremum.dynamic.resources;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;

@Slf4j
public class LocalResourceLoader implements ResourceLoader {
    @Override
    public InputStream loadAsInputStream(Path path) {
        try {
            return new FileInputStream(new File(path.toString()));
        } catch (FileNotFoundException e) {
            log.error("File {} not found", path, e);
            throw new RuntimeException("File " + path.toString() + " can't be loaded");
        }
    }
}
