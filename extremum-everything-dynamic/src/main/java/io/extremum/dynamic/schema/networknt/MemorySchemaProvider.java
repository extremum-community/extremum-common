package io.extremum.dynamic.schema.networknt;

import io.extremum.dynamic.MemorySchemaPointer;
import io.extremum.dynamic.schema.SchemaProvider;

public class MemorySchemaProvider implements SchemaProvider<NetworkntSchema, MemorySchemaPointer> {
    @Override
    public NetworkntSchema loadSchema(MemorySchemaPointer schemaPointer) {
        throw new UnsupportedOperationException("Not supported yet");
    }
}
