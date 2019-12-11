package io.extremum.dynamic.schema.networknt;

import com.networknt.schema.JsonMetaSchema;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaException;
import com.networknt.schema.JsonSchemaFactory;
import io.extremum.dynamic.resources.ResourceLoader;
import io.extremum.dynamic.resources.exceptions.ResourceNotFoundException;
import io.extremum.dynamic.schema.JsonSchemaType;
import io.extremum.dynamic.validator.exceptions.SchemaNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@RequiredArgsConstructor
public class FileSystemNetworkntSchemaProvider implements NetworkntSchemaProvider {
    private final JsonSchemaType type;
    private final ResourceLoader resourceLoader;
    private final Path schemaDirectory;

    @Override
    @SneakyThrows
    public NetworkntSchema loadSchema(String schemaName) throws SchemaNotFoundException {
        JsonSchemaFactory factory = createFactory(type, schemaDirectory);

        Path schemaPath = Paths.get(schemaDirectory.toString(), schemaName);

        InputStream is = null;
        try {
            is = resourceLoader.loadAsInputStream(schemaPath);
            JsonSchema schema = factory.getSchema(is);
            return new NetworkntSchema(schema);
        } catch (ResourceNotFoundException e) {
            log.error("Schema {} doesn't found in path {}", schemaName, schemaDirectory);
            throw new SchemaNotFoundException(schemaName, e);
        } catch (JsonSchemaException e) {
            Throwable cause = e.getCause();
            if (cause instanceof SchemaNotFoundException) {
                throw new SchemaNotFoundException(schemaName, cause);
            } else {
                throw new JsonSchemaException(e);
            }
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    protected JsonSchemaFactory createFactory(JsonSchemaType type, Path basicDirectory) {
        if (JsonSchemaType.V2019_09.equals(type)) {
            JsonMetaSchema metaSchema = JsonMetaSchema.getV201909();
            return new JsonSchemaFactory.Builder()
                    .addMetaSchema(metaSchema)
                    .defaultMetaSchemaURI(metaSchema.getUri())
                    .uriFetcher(uri -> {
                        String fileName = uri.toString().substring(uri.getScheme().length() + 1);
                        Path path = Paths.get(basicDirectory.toString(), fileName);
                        try {
                            return resourceLoader.loadAsInputStream(path);
                        } catch (ResourceNotFoundException e) {
                            log.error("Schema {} doesn't found in path {}", fileName, basicDirectory);
                            throw new SchemaNotFoundException(fileName, e);
                        }
                    }, "file")
                    .build();
        } else {
            throw new RuntimeException("Only " + JsonSchemaType.V2019_09 + " schema version is supported");
        }
    }
}
