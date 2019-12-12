package io.extremum.dynamic.schema.provider.networknt;

import io.extremum.dynamic.schema.networknt.NetworkntSchema;
import io.extremum.dynamic.validator.exceptions.SchemaNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.mockito.Mockito.*;

class CachingNetworkntSchemaProviderTest {
    @Test
    void schemaLoadsFromCacheFirst() {
        CachingNetworkntSchemaProvider provider = spy(CachingNetworkntSchemaProvider.class);
        String schemaName = "schemaName";

        when(provider.loadFromCache(schemaName)).thenReturn(Optional.of(Mockito.mock(NetworkntSchema.class)));

        provider.loadSchema(schemaName);

        verify(provider, times(1)).loadFromCache(schemaName);
        verify(provider, never()).fetchSchema(any());
    }

    @Test
    void schemaLoadsFromSource_ifCacheDoesntContainASchema_and_schemaCached() {
        CachingNetworkntSchemaProvider provider = spy(CachingNetworkntSchemaProvider.class);
        String schemaName = "schemaName";

        when(provider.loadFromCache(schemaName)).thenReturn(Optional.empty());
        when(provider.fetchSchema(schemaName)).thenReturn(Optional.of(Mockito.mock(NetworkntSchema.class)));

        provider.loadSchema(schemaName);

        verify(provider, times(1)).fetchSchema(schemaName);
        verify(provider, times(1)).putToCache(schemaName);
    }

    @Test
    void exceptionThrown_when_schemaIsNotFound() {
        CachingNetworkntSchemaProvider provider = spy(CachingNetworkntSchemaProvider.class);
        String schemaName = "schemaName";

        when(provider.loadFromCache(schemaName)).thenReturn(Optional.empty());
        when(provider.fetchSchema(schemaName)).thenReturn(Optional.empty());

        Assertions.assertThrows(SchemaNotFoundException.class, () -> provider.loadSchema(schemaName));

        verify(provider, times(1)).loadFromCache(schemaName);
        verify(provider, times(1)).fetchSchema(schemaName);
        verify(provider, never()).putToCache(any());

    }
}
