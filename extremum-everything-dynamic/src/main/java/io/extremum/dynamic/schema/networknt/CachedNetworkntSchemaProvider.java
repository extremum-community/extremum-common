package io.extremum.dynamic.schema.networknt;

import io.extremum.dynamic.validator.exceptions.SchemaNotFoundException;

import java.util.Optional;
import java.util.function.Supplier;

public abstract class CachedNetworkntSchemaProvider implements NetworkntSchemaProvider {
    @Override
    public NetworkntSchema loadSchema(String schemaName) throws SchemaNotFoundException {
        Optional<NetworkntSchema> loaded = loadFromCache(schemaName);

        return loaded.orElseGet(fetchSchemaFromSource(schemaName));
    }

    private Supplier<NetworkntSchema> fetchSchemaFromSource(String schemaName) {
        return () -> fetchSchema(schemaName)
                .map(s -> {
                    putToCache(schemaName);
                    return s;
                })
                .orElseThrow(() -> new SchemaNotFoundException(schemaName));
    }

    abstract protected void putToCache(String schemaName);

    abstract protected Optional<NetworkntSchema> fetchSchema(String pointer);

    abstract protected Optional<NetworkntSchema> loadFromCache(String schema);
}
