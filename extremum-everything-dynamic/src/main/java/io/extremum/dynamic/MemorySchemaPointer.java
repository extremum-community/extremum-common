package io.extremum.dynamic;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MemorySchemaPointer implements SchemaPointer<String> {
    private final String pointer;

    @Override
    public String getPointer() {
        return pointer;
    }
}
