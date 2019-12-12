package io.extremum.dynamic.schema.provider.networknt.caching;

import io.extremum.dynamic.schema.networknt.NetworkntSchema;

import java.util.Optional;

public interface NetworkntCacheManager {
    void cacheSchema(NetworkntSchema cachedSchema, String schemaName);

    Optional<NetworkntSchema> fetchFromCache(String schemaName);
}
