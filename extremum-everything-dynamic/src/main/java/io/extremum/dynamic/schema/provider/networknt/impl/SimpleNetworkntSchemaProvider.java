package io.extremum.dynamic.schema.provider.networknt.impl;

import io.extremum.dynamic.resources.NetworkntUrlFetcherExternalResourceLoader;
import io.extremum.dynamic.resources.ResourceLoader;
import io.extremum.dynamic.schema.JsonSchemaType;
import io.extremum.dynamic.schema.provider.networknt.AbstractNetworkntSchemaProvider;
import io.extremum.dynamic.schema.provider.networknt.NetworkntURIFetcher;

import java.net.URI;
import java.util.Collections;
import java.util.List;

public class SimpleNetworkntSchemaProvider extends AbstractNetworkntSchemaProvider {
    private final String basePath;

    public SimpleNetworkntSchemaProvider(JsonSchemaType type, String basePath) {
        super(type);
        this.basePath = basePath;
    }

    @Override
    protected ResourceLoader getResourceLoader() {
        return new NetworkntUrlFetcherExternalResourceLoader();
    }

    @Override
    protected List<NetworkntURIFetcher> getUriFetchers() {
        return Collections.emptyList();
    }

    @Override
    protected URI makeSchemaUri(String schemaName) {
        return URI.create(basePath).resolve(schemaName);
    }
}
