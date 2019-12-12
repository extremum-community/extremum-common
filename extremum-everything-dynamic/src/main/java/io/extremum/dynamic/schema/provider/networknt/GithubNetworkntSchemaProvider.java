package io.extremum.dynamic.schema.provider.networknt;

import com.networknt.schema.JsonMetaSchema;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import io.extremum.dynamic.schema.JsonSchemaType;
import io.extremum.dynamic.schema.networknt.NetworkntSchema;
import io.extremum.dynamic.validator.exceptions.SchemaLoadingException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GithubNetworkntSchemaProvider implements NetworkntSchemaProvider {
    private final JsonSchemaType type;
    private final GithubConfiguration githubConfiguration;

    @Override
    public NetworkntSchema loadSchema(String schemaName) throws SchemaLoadingException {
        JsonSchemaFactory factory = createFactory();

//        loadFileFromJsonAsString(Paths.get(githubConfiguration.getSchemaBasePath(), schemaName));

        JsonSchema schema = factory.getSchema(githubConfiguration.getSchemaBasePath());
        return new NetworkntSchema(schema);
    }

    protected JsonSchemaFactory createFactory() {
        if (JsonSchemaType.V2019_09.equals(type)) {
            JsonMetaSchema metaSchema = JsonMetaSchema.getV201909();
            return new JsonSchemaFactory.Builder()
                    .addMetaSchema(metaSchema)
                    .defaultMetaSchemaURI(metaSchema.getUri())
//                    .uriFetcher()
                    .build();
        } else {
            throw new RuntimeException("Only " + JsonSchemaType.V2019_09 + " schema version is supported");
        }
    }

    @Override
    public JsonSchemaType getSchemaType() {
        return type;
    }
}
