package io.extremum.dynamic;

import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DirectorySchemaPointerTest {
    @Test
    void createOverConstructor() {
        String pathToFile = "/aa/bb/cc/file.txt";
        DirectorySchemaPointer pointer = new DirectorySchemaPointer(Paths.get(pathToFile));
        assertEquals(pathToFile, pointer.getPointer().toString());
    }

    @Test
    void createFromString() {
        String pathToFile = "/aa/bb/cc/file.txt";
        DirectorySchemaPointer pointer = DirectorySchemaPointer.createFromString(pathToFile);
        assertEquals(pathToFile, pointer.getPointer().toString());
    }
}
