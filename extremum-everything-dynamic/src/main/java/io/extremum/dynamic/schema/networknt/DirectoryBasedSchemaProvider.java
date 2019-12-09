package io.extremum.dynamic.schema.networknt;

import com.networknt.schema.JsonMetaSchema;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.uri.URIFetcher;
import io.extremum.dynamic.resources.ResourceLoader;
import io.extremum.dynamic.schema.JsonSchemaType;
import io.extremum.dynamic.schema.SchemaProvider;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

@RequiredArgsConstructor
public class DirectoryBasedSchemaProvider implements SchemaProvider<NetworkntSchema> {
    private final Path baseDirectory;
    private final JsonSchemaType type;
    private final ResourceLoader resourceLoader;

    @Override
    public NetworkntSchema loadSchema(String relativeSchemaPath) {
        JsonSchemaFactory factory = createFactory(type);
        InputStream is = resourceLoader.loadAsInputStream(Paths.get(baseDirectory.toString(), relativeSchemaPath));
        JsonSchema schema = factory.getSchema(is);
        return new NetworkntSchema(schema);
    }

    protected JsonSchemaFactory createFactory(JsonSchemaType type) {
        if (JsonSchemaType.V2019_09.equals(type)) {
            JsonMetaSchema metaSchema = JsonMetaSchema.getV201909();
            return new JsonSchemaFactory.Builder()
                    .addMetaSchema(metaSchema)
                    .defaultMetaSchemaURI(metaSchema.getUri())
                    .uriFetcher(new URIFetcher() {
                        @Override
                        public InputStream fetch(URI uri) throws IOException {
                            String fileName = uri.toString().substring(uri.getScheme().length() + 1);
                            Path path = Paths.get(baseDirectory.toString(), fileName);
                            return resourceLoader.loadAsInputStream(path);
                        }
                    }, "file")
                    .build();
        } else {
            throw new RuntimeException("Only " + JsonSchemaType.V2019_09 + " schema version is supported");
        }
    }
}
