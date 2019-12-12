package io.extremum.dynamic.resources;

import io.extremum.dynamic.TestUtils;
import io.extremum.dynamic.resources.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

class LocalResourceLoaderTest {
    @Test
    void loadLocalResource() throws ResourceNotFoundException {
        String path = this.getClass().getClassLoader().getResource("test.file.txt").getPath();

        LocalResourceLoader resourceLoader = new LocalResourceLoader();
        InputStream inputStream = resourceLoader.loadAsInputStream(URI.create("file:/").resolve(Paths.get(path).toString()));

        Assertions.assertNotNull(inputStream);

        String textFromLocalResource = TestUtils.convertInputStreamToString(inputStream);
        Assertions.assertNotNull(textFromLocalResource);
        Assertions.assertEquals("abcd", textFromLocalResource);
    }

    @Test
    void loadUnknownResource_throwsResourceNotFoundException() {
        Path unknownPath = Paths.get("unknown_path");
        LocalResourceLoader resourceLoader = new LocalResourceLoader();
        Assertions.assertThrows(ResourceNotFoundException.class, () -> resourceLoader.loadAsInputStream(
                URI.create("file:/").resolve(unknownPath.toString())));
    }
}
