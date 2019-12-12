package io.extremum.dynamic.schema.networknt;

import io.extremum.dynamic.validator.exceptions.SchemaNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.mockito.Mockito.*;

class CachedNetworkntSchemaProviderTest {
    @Test
    void schemaLoadsFromCacheFirst() {
        CachedNetworkntSchemaProvider provider = spy(CachedNetworkntSchemaProvider.class);
        String schemaName = "schemaName";

        when(provider.loadFromCache(schemaName)).thenReturn(Optional.of(Mockito.mock(NetworkntSchema.class)));

        provider.loadSchema(schemaName);

        verify(provider, times(1)).loadFromCache(schemaName);
        verify(provider, never()).fetchSchema(any());
    }

    @Test
    void schemaLoadsFromSource_ifCacheDoesntContainASchema_and_schemaCached() {
        CachedNetworkntSchemaProvider provider = spy(CachedNetworkntSchemaProvider.class);
        String schemaName = "schemaName";

        when(provider.loadFromCache(schemaName)).thenReturn(Optional.empty());
        when(provider.fetchSchema(schemaName)).thenReturn(Optional.of(Mockito.mock(NetworkntSchema.class)));

        provider.loadSchema(schemaName);

        verify(provider, times(1)).fetchSchema(schemaName);
        verify(provider, times(1)).putToCache(schemaName);
    }

    @Test
    void exceptionThrown_when_schemaIsNotFound() {
        CachedNetworkntSchemaProvider provider = spy(CachedNetworkntSchemaProvider.class);
        String schemaName = "schemaName";

        when(provider.loadFromCache(schemaName)).thenReturn(Optional.empty());
        when(provider.fetchSchema(schemaName)).thenReturn(Optional.empty());

        Assertions.assertThrows(SchemaNotFoundException.class, () -> provider.loadSchema(schemaName));

        verify(provider, times(1)).loadFromCache(schemaName);
        verify(provider, times(1)).fetchSchema(schemaName);
        verify(provider, never()).putToCache(any());

    }
}
