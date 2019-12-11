package io.extremum.dynamic.resources;

import io.extremum.dynamic.Utils;
import io.extremum.dynamic.resources.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

class LocalResourceLoaderTest {
    @Test
    void loadLocalResources() throws ResourceNotFoundException {
        String path = Thread.currentThread().getContextClassLoader().getResource("test.file.txt").getPath();

        LocalResourceLoader resourceLoader = new LocalResourceLoader();
        InputStream inputStream = resourceLoader.loadAsInputStream(Paths.get(path));

        Assertions.assertNotNull(inputStream);

        String textFromLocalResource = Utils.convertInputStreamToString(inputStream);
        Assertions.assertNotNull(textFromLocalResource);
        Assertions.assertEquals("abcd", textFromLocalResource);
    }

    @Test
    void loadUnknownResources_throwsException() {
        Path unknownPath = Paths.get("unknown path");
        LocalResourceLoader resourceLoader = new LocalResourceLoader();
        Assertions.assertThrows(ResourceNotFoundException.class, () -> resourceLoader.loadAsInputStream(unknownPath));
    }
}
