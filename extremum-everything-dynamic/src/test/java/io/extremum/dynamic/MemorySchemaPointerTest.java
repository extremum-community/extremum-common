package io.extremum.dynamic;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MemorySchemaPointerTest {
    @Test
    void getPointerTest() {
        String pointer = "pointer";
        MemorySchemaPointer p = new MemorySchemaPointer(pointer);

        Assertions.assertEquals(pointer, p.getPointer());
    }
}
