package io.extremum.dynamic.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.ValidationMessage;
import io.extremum.dynamic.resources.LocalResourceLoader;
import io.extremum.dynamic.resources.ResourceLoader;
import io.extremum.dynamic.schema.networknt.FileSystemNetworkntSchemaProvider;
import io.extremum.dynamic.schema.networknt.NetworkntSchema;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FileSystemNetworkntSchemaProviderTest {
    static ResourceLoader localResourceLoader = new LocalResourceLoader();
    static FileSystemNetworkntSchemaProvider provider;

    @BeforeAll
    static void before() {
        String pathToFile = Thread.currentThread().getContextClassLoader().getResource("test.file.txt").getPath();
        String base = pathToFile.substring(0, pathToFile.lastIndexOf("/"));

        provider = new FileSystemNetworkntSchemaProvider(JsonSchemaType.V2019_09, localResourceLoader,
                Paths.get(base, "schemas"));
    }

    @Test
    void schemaLoadedFromLocalFileSystemOkTest() {
        assertDoesNotThrow(() -> provider.loadSchema("simple.schema.json"));
    }

    @Test
    void loadedSchemaContainsAJsonSchemaDataTest() {
        NetworkntSchema schema = provider.loadSchema("simple.schema.json");
        assertEquals("an_id_for_simple_schema_json",
                schema.getSchema().getSchemaNode().get("$id").textValue());
    }

    @Test
    void loadsSchemaWithRefsToAnotherSchemaInLocalFileSystemOkTest() throws IOException {
        NetworkntSchema schema = provider.loadSchema("complex.schema.json");

        assertEquals("an_id_for_complex_schema_json",
                schema.getSchema().getSchemaNode().get("$id").textValue());

        ObjectMapper mapper = new ObjectMapper();

        String modelData = "{\"field1\":\"sss\", \"field2\":33.2, \"field3\":23}";
        JsonNode jsonNode = mapper.readValue(modelData, JsonNode.class);
        Set<ValidationMessage> violations = schema.getSchema().validate(jsonNode);

        Assertions.assertFalse(violations.isEmpty());
    }
}
