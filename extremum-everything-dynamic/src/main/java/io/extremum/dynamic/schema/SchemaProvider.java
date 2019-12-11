package io.extremum.dynamic.schema;

import io.extremum.dynamic.validator.exceptions.SchemaNotFoundException;

public interface SchemaProvider<S extends Schema<?>> {
    S loadSchema(String schemaName) throws SchemaNotFoundException;
}
