package io.extremum.dynamic.schema;

import io.extremum.dynamic.SchemaPointer;

public interface SchemaProvider<S extends Schema<?>, P extends SchemaPointer<?>> {
    S loadSchema(P schemaPointer);
}
