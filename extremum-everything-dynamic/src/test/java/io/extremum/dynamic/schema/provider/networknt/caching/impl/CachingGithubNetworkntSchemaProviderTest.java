package io.extremum.dynamic.schema.provider.networknt.caching.impl;

import io.extremum.dynamic.schema.networknt.NetworkntSchema;
import io.extremum.dynamic.schema.provider.networknt.caching.CachingNetworkntSchemaProvider;
import io.extremum.dynamic.schema.provider.networknt.caching.NetworkntCacheManager;
import io.extremum.dynamic.schema.provider.networknt.impl.GithubNetworkntSchemaProvider;
import io.extremum.dynamic.validator.exceptions.SchemaLoadingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.Mockito.*;

class CachingGithubNetworkntSchemaProviderTest {
    private GithubNetworkntSchemaProvider githuSchemaProvider = mock(GithubNetworkntSchemaProvider.class);

    @Test
    void loadFromCacheFirst() {
        NetworkntCacheManager manager = mock(NetworkntCacheManager.class);

        CachingNetworkntSchemaProvider provider = spy(new CachingGithubNetworkntSchemaProvider(manager, githuSchemaProvider));

        String pointer = "pointer";

        when(manager.fetchFromCache(pointer)).thenReturn(Optional.of(mock(NetworkntSchema.class)));

        NetworkntSchema provided = provider.loadSchema(pointer);

        verify(manager).fetchFromCache(pointer);
        verify(githuSchemaProvider, never()).loadSchema(anyString());

        Assertions.assertNotNull(provided);
    }

    @Test
    void loadFromSourceIfCacheDoesntHoldAValue() {
        NetworkntCacheManager manager = mock(NetworkntCacheManager.class);

        CachingNetworkntSchemaProvider provider = spy(new CachingGithubNetworkntSchemaProvider(manager, githuSchemaProvider));

        String pointer = "pointer";

        when(manager.fetchFromCache(pointer)).thenReturn(Optional.empty());
        when(githuSchemaProvider.loadSchema(pointer)).thenReturn(mock(NetworkntSchema.class));

        NetworkntSchema provided = provider.loadSchema(pointer);

        verify(manager).fetchFromCache(pointer);
        verify(githuSchemaProvider).loadSchema(anyString());

        Assertions.assertNotNull(provided);
    }

    @Test
    void throw_SchemaLoadingException_if_schemaNotLoaded() {
        NetworkntCacheManager manager = mock(NetworkntCacheManager.class);

        CachingNetworkntSchemaProvider provider = spy(new CachingGithubNetworkntSchemaProvider(manager, githuSchemaProvider));

        String pointer = "pointer";

        when(manager.fetchFromCache(pointer)).thenReturn(Optional.empty());
        when(githuSchemaProvider.loadSchema(pointer)).thenThrow(SchemaLoadingException.class);

        Assertions.assertThrows(SchemaLoadingException.class, () -> provider.loadSchema(pointer));

        verify(manager).fetchFromCache(pointer);
        verify(githuSchemaProvider).loadSchema(anyString());
    }
}