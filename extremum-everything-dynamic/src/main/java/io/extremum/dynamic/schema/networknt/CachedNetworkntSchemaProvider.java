package io.extremum.dynamic.schema.networknt;

import io.extremum.dynamic.validator.exceptions.SchemaNotFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class CachedNetworkntSchemaProvider implements NetworkntSchemaProvider {
    private final Map<String, NetworkntSchema> schemas = new HashMap<>();

    @Override
    public NetworkntSchema loadSchema(String schemaName) throws SchemaNotFoundException {
        Optional<NetworkntSchema> loaded = loadFromMemory(schemaName);
        if (loaded.isPresent()) {
            return loaded.get();
        } else {
            Optional<NetworkntSchema> newSchema = getSchema(schemaName);
            newSchema.ifPresent(schema -> schemas.put(schemaName, schema));
            return newSchema.orElseThrow(() -> new SchemaNotFoundException(schemaName));
        }
    }

    abstract protected Optional<NetworkntSchema> getSchema(String pointer);

    protected Optional<NetworkntSchema> loadFromMemory(String pointer) {
        return Optional.of(schemas.get(pointer));
    }
}
