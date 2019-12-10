package io.extremum.dynamic.schema.networknt;

import io.extremum.dynamic.MongoSchemaPointer;
import io.extremum.dynamic.schema.SchemaProvider;

public class MongoBasedSchemaProvider implements SchemaProvider<NetworkntSchema, MongoSchemaPointer> {
    @Override
    public NetworkntSchema loadSchema(MongoSchemaPointer schemaPointer) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
