package io.extremum.dynamic.schema.provider.networknt.caching;

import io.extremum.dynamic.schema.networknt.NetworkntSchema;
import io.extremum.dynamic.schema.provider.networknt.NetworkntSchemaProvider;
import io.extremum.dynamic.validator.exceptions.SchemaLoadingException;
import io.extremum.dynamic.validator.exceptions.SchemaNotFoundException;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.function.Supplier;

@RequiredArgsConstructor
public abstract class CachingNetworkntSchemaProvider implements NetworkntSchemaProvider {
    private final NetworkntCacheManager cacheManager;

    @Override
    public NetworkntSchema loadSchema(String schemaName) throws SchemaLoadingException {
        Optional<NetworkntSchema> loaded = cacheManager.fetchFromCache(schemaName);

        return loaded.orElseGet(fetchSchemaFromSource(schemaName));
    }

    private Supplier<NetworkntSchema> fetchSchemaFromSource(String schemaName) {
        return () -> fetchSchemaForcibly(schemaName)
                .map(s -> {
                    cacheManager.cacheSchema(s, schemaName);
                    return s;
                })
                .orElseThrow(() -> new SchemaNotFoundException(schemaName));
    }

    abstract protected Optional<NetworkntSchema> fetchSchemaForcibly(String pointer);
}
