package io.extremum.dynamic.validator.exceptions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SchemaNotFoundExceptionTest {
    @Test
    void getSchemaName() {
        String schemaName = "schemaName";
        SchemaNotFoundException ex = new SchemaNotFoundException(schemaName);

        Assertions.assertEquals(schemaName, ex.getSchemaName());
    }
}
