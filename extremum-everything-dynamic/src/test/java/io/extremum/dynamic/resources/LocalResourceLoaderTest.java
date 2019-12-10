package io.extremum.dynamic.resources;

import io.extremum.dynamic.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.file.Paths;

class LocalResourceLoaderTest {
    @Test
    void loadLocalResources() {
        String path = Thread.currentThread().getContextClassLoader().getResource("test.file.txt").getPath();

        LocalResourceLoader resourceLoader = new LocalResourceLoader();
        InputStream inputStream = resourceLoader.loadAsInputStream(Paths.get(path));

        Assertions.assertNotNull(inputStream);

        String textFromLocalResource = Utils.convertInputStreamToString(inputStream);
        Assertions.assertNotNull(textFromLocalResource);
        Assertions.assertEquals("abcd", textFromLocalResource);
    }
}
