package io.extremum.dynamic.schema;

public interface SchemaProvider<S extends Schema<?>> {
    S loadSchema(String path);
}
