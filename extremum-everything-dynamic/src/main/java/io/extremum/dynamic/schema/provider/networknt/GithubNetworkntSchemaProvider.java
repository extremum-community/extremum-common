package io.extremum.dynamic.schema.provider.networknt;

import com.networknt.schema.JsonMetaSchema;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.uri.URIFactory;
import io.extremum.dynamic.resources.github.GithubAccessOptions;
import io.extremum.dynamic.resources.github.GithubResourceConfiguration;
import io.extremum.dynamic.resources.github.GithubResourceLoader;
import io.extremum.dynamic.schema.JsonSchemaType;
import io.extremum.dynamic.schema.networknt.NetworkntSchema;
import io.extremum.dynamic.validator.exceptions.SchemaLoadingException;
import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

@RequiredArgsConstructor
public class GithubNetworkntSchemaProvider implements NetworkntSchemaProvider {
    private final JsonSchemaType type;
    private final GithubResourceConfiguration githubResourceConfiguration;
    private final GithubAccessOptions githubAccessOptions;

    @Override
    public JsonSchemaType getSchemaType() {
        return type;
    }

    @Override
    public NetworkntSchema loadSchema(String schemaName) throws SchemaLoadingException {
        JsonMetaSchema metaSchema = JsonMetaSchema.getV201909();

        GithubResourceLoader loader = new GithubResourceLoader(githubAccessOptions);

        ResourceLoaderBasedUriFetcher uriFetcher = new ResourceLoaderBasedUriFetcher(loader);
        URIFactory uriFactory = new QueryParamsPreservingURIFactory();

        JsonSchemaFactory factory = new JsonSchemaFactory.Builder()
                .addMetaSchema(metaSchema)
                .defaultMetaSchemaURI(metaSchema.getUri())
                .uriFetcher(uriFetcher, ResourceLoaderBasedUriFetcher.SUPPORTED_SCHEMES.toArray(new String[] {}))
                .uriFactory(uriFactory, QueryParamsPreservingURIFactory.SUPPORTED_SCHEMES.toArray(new String[] {}))
                .build();

        JsonSchema schema = factory.getSchema(makeUrl(schemaName, githubResourceConfiguration));

        return new NetworkntSchema(schema);
    }

    private URI makeUrl(String schemaName, GithubResourceConfiguration conf) {
        Path path = Paths.get("/repos",
                conf.getOwner(),
                conf.getRepo(),
                "/contents",
                conf.getSchemaPath(),
                String.format("%s?ref=%s", schemaName, conf.getRef()));

        return URI.create(conf.getGithubApiBase()).resolve(path.toString());
    }
}
