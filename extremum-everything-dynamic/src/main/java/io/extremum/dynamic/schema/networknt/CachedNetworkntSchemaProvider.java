package io.extremum.dynamic.schema.networknt;

import io.extremum.dynamic.validator.exceptions.SchemaNotFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class CachedNetworkntSchemaProvider implements NetworkntSchemaProvider {
    private final Map<String, NetworkntSchema> schemas = new HashMap<>();

    @Override
    public NetworkntSchema loadSchema(String schemaName) throws SchemaNotFoundException {
        Optional<NetworkntSchema> loaded = loadFromMemory(schemaName);

        return loaded.orElseGet(fetchSchemaFromSource(schemaName));
    }

    protected Supplier<NetworkntSchema> fetchSchemaFromSource(String schemaName) {
        return () -> getSchema(schemaName)
                .map(putToCache(schemaName))
                .orElseThrow(() -> new SchemaNotFoundException(schemaName));
    }

    private Function<NetworkntSchema, NetworkntSchema> putToCache(String schemaName) {
        return s -> {
            schemas.put(schemaName, s);
            return s;
        };
    }

    abstract protected Optional<NetworkntSchema> getSchema(String pointer);

    protected Optional<NetworkntSchema> loadFromMemory(String pointer) {
        return Optional.of(schemas.get(pointer));
    }
}
