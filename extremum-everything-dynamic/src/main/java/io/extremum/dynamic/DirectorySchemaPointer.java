package io.extremum.dynamic;

import java.nio.file.Path;
import java.nio.file.Paths;

public class DirectorySchemaPointer implements SchemaPointer<Path> {
    private Path pointer;

    public static DirectorySchemaPointer createFromString(String pointer) {
        DirectorySchemaPointer p = new DirectorySchemaPointer();
        p.pointer = Paths.get(pointer);
        return p;
    }

    @Override
    public Path getPointer() {
        return pointer;
    }
}
