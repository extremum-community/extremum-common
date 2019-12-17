package io.extremum.dynamic.schema.provider.networknt;

import com.networknt.schema.JsonMetaSchema;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaException;
import com.networknt.schema.JsonSchemaFactory;
import io.extremum.dynamic.resources.ResourceLoader;
import io.extremum.dynamic.resources.exceptions.ResourceLoadingException;
import io.extremum.dynamic.resources.exceptions.ResourceNotFoundException;
import io.extremum.dynamic.schema.JsonSchemaType;
import io.extremum.dynamic.schema.networknt.NetworkntSchema;
import io.extremum.dynamic.validator.exceptions.SchemaLoadingException;
import io.extremum.dynamic.validator.exceptions.SchemaNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractNetworkntSchemaProvider implements NetworkntSchemaProvider {
    private final JsonSchemaType type;

    @Override
    public NetworkntSchema loadSchema(String schemaName) throws SchemaLoadingException {
        JsonSchemaFactory factory = createFactory(type, getUriFetchers());

        URI schemaUri = makeSchemaUri(schemaName);

        try (InputStream is = getResourceLoader().loadAsInputStream(schemaUri)) {
            JsonSchema schema = factory.getSchema(is);
            return new NetworkntSchema(schema);
        } catch (ResourceLoadingException e) {
            log.error("Unable to load schema {} from uri {}", schemaName, schemaUri);
            if (e instanceof ResourceNotFoundException) {
                throw new SchemaNotFoundException(schemaName, e);
            } else {
                throw new SchemaLoadingException(schemaName, e);
            }
        } catch (JsonSchemaException e) {
            Throwable cause = e.getCause();
            if (cause instanceof SchemaNotFoundException) {
                log.error("Schema " + schemaName + " isn't found");
                throw new SchemaNotFoundException(schemaName, cause);
            } else {
                String errMessage = "Unhandled " + e.getClass() + " exception was thrown and will be rethrown." +
                        "Schema " + schemaName + " can't be provided";

                log.error(errMessage);

                throw new JsonSchemaException(e);
            }
        } catch (IOException e) {
            String errMessage = "Unhandled " + e.getClass() + " exception was thrown and will be rethrown as RuntimeException." +
                    "Schema " + schemaName + " can't be provided";

            log.error(errMessage, e);

            throw new RuntimeException(errMessage, e);
        }
    }

    protected abstract ResourceLoader getResourceLoader();

    protected abstract List<NetworkntURIFetcher> getUriFetchers();

    protected abstract URI makeSchemaUri(String schemaName);

    private JsonSchemaFactory createFactory(JsonSchemaType type, List<NetworkntURIFetcher> uriFetchers) {
        if (JsonSchemaType.V2019_09.equals(type)) {
            JsonMetaSchema metaSchema = JsonMetaSchema.getV201909();
            JsonSchemaFactory.Builder builder = new JsonSchemaFactory.Builder()
                    .addMetaSchema(metaSchema)
                    .defaultMetaSchemaURI(metaSchema.getUri());

            uriFetchers.forEach(fetcher ->
                    builder.uriFetcher(fetcher, fetcher.getSupportedSchemas().toArray(new String[]{})));

            return builder.build();
        } else {
            throw new RuntimeException("Only " + JsonSchemaType.V2019_09 + " schema version is supported");
        }
    }

    @Override
    public JsonSchemaType getSchemaType() {
        return type;
    }
}
