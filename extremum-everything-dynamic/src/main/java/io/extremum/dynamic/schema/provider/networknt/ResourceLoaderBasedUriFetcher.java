package io.extremum.dynamic.schema.provider.networknt;

import com.networknt.schema.uri.URIFetcher;
import io.extremum.dynamic.resources.ResourceLoader;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
public class ResourceLoaderBasedUriFetcher implements URIFetcher {
    public static final Set<String> SUPPORTED_SCHEMES = Collections.unmodifiableSet(new HashSet<>(
            Arrays.asList("http", "https")));

    private final ResourceLoader resourceLoader;

    @Override
    public InputStream fetch(URI uri) throws IOException {
        return resourceLoader.loadAsInputStream(uri);
    }
}
