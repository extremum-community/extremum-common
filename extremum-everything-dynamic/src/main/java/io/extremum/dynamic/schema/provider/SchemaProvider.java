package io.extremum.dynamic.schema.provider;

import io.extremum.dynamic.schema.Schema;
import io.extremum.dynamic.validator.exceptions.SchemaNotFoundException;

public interface SchemaProvider<S extends Schema<?>> {
    S loadSchema(String schemaName) throws SchemaNotFoundException;
}
