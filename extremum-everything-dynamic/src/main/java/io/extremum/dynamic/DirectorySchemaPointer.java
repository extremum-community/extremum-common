package io.extremum.dynamic;

import lombok.AllArgsConstructor;

import java.nio.file.Path;
import java.nio.file.Paths;

@AllArgsConstructor
public class DirectorySchemaPointer implements SchemaPointer<Path> {
    private final Path pointer;

    public static DirectorySchemaPointer createFromString(String pointer) {
        return new DirectorySchemaPointer(Paths.get(pointer));
    }

    @Override
    public Path getPointer() {
        return pointer;
    }
}
