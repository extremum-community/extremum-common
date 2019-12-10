package io.extremum.dynamic.schema.networknt;

import com.networknt.schema.JsonMetaSchema;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.uri.URIFetcher;
import io.extremum.dynamic.DirectorySchemaPointer;
import io.extremum.dynamic.resources.ResourceLoader;
import io.extremum.dynamic.schema.JsonSchemaType;
import io.extremum.dynamic.schema.SchemaProvider;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@RequiredArgsConstructor
public class DirectoryBasedSchemaProvider implements SchemaProvider<NetworkntSchema, DirectorySchemaPointer> {
    private final JsonSchemaType type;
    private final ResourceLoader resourceLoader;

    @Override
    @SneakyThrows
    public NetworkntSchema loadSchema(DirectorySchemaPointer schemaPointer) {
        Path pointer = schemaPointer.getPointer();
        Path baseDirectory = pointer.getParent();

        JsonSchemaFactory factory = createFactory(type, baseDirectory);
        try (InputStream is = resourceLoader.loadAsInputStream(pointer)) {
            JsonSchema schema = factory.getSchema(is);
            return new NetworkntSchema(schema);
        }
    }

    protected JsonSchemaFactory createFactory(JsonSchemaType type, Path basicDirectory) {
        if (JsonSchemaType.V2019_09.equals(type)) {
            JsonMetaSchema metaSchema = JsonMetaSchema.getV201909();
            return new JsonSchemaFactory.Builder()
                    .addMetaSchema(metaSchema)
                    .defaultMetaSchemaURI(metaSchema.getUri())
                    .uriFetcher(new URIFetcher() {
                        @Override
                        public InputStream fetch(URI uri) throws IOException {
                            String fileName = uri.toString().substring(uri.getScheme().length() + 1);
                            Path path = Paths.get(basicDirectory.toString(), fileName);
                            return resourceLoader.loadAsInputStream(path);
                        }
                    }, "file")
                    .build();
        } else {
            throw new RuntimeException("Only " + JsonSchemaType.V2019_09 + " schema version is supported");
        }
    }
}
