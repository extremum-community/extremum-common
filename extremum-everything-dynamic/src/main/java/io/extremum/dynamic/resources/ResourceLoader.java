package io.extremum.dynamic.resources;

import java.io.InputStream;
import java.nio.file.Path;

public interface ResourceLoader {
    InputStream loadAsInputStream(Path path);
}
