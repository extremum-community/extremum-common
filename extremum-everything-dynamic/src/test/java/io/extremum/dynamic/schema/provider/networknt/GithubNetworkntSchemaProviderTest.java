package io.extremum.dynamic.schema.provider.networknt;

import io.extremum.dynamic.schema.JsonSchemaType;
import io.extremum.dynamic.schema.networknt.NetworkntSchema;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class GithubNetworkntSchemaProviderTest {
    @Disabled
    @Test
    void loadSchema() {
        GithubConfiguration properties = new GithubConfiguration(
                "jonua",
                "schemas",
                "https://github.com/jonua/schemas/tree/master/path/to/schemas");
        NetworkntSchemaProvider provider = new GithubNetworkntSchemaProvider(JsonSchemaType.V2019_09, properties);
        NetworkntSchema schema = provider.loadSchema("main");

        System.out.println(schema);
    }
}