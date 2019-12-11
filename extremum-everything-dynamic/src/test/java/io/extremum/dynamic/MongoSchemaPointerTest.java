package io.extremum.dynamic;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MongoSchemaPointerTest {
    @Test
    void createOverConstructor() {
        UUID rawPointer = UUID.randomUUID();
        MongoSchemaPointer pointer = new MongoSchemaPointer(rawPointer);
        assertEquals(rawPointer, pointer.getPointer());
    }

    @Test
    void createFromString() {
        UUID rawPointer = UUID.randomUUID();
        MongoSchemaPointer pointer = MongoSchemaPointer.createFromString(rawPointer.toString());
        assertEquals(rawPointer, pointer.getPointer());
    }
}
