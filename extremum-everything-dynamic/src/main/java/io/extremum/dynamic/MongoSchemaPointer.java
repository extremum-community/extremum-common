package io.extremum.dynamic;

import lombok.AllArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
public class MongoSchemaPointer implements SchemaPointer<UUID> {
    private final UUID pointer;

    public static MongoSchemaPointer createFromString(String stringPointer) {
        return new MongoSchemaPointer(UUID.fromString(stringPointer));
    }

    @Override
    public UUID getPointer() {
        return pointer;
    }
}
