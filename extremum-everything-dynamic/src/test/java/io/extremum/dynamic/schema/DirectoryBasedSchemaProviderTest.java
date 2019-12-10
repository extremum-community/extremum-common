package io.extremum.dynamic.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.ValidationMessage;
import io.extremum.dynamic.DirectorySchemaPointer;
import io.extremum.dynamic.resources.LocalResourceLoader;
import io.extremum.dynamic.resources.ResourceLoader;
import io.extremum.dynamic.schema.networknt.DirectoryBasedSchemaProvider;
import io.extremum.dynamic.schema.networknt.NetworkntSchema;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DirectoryBasedSchemaProviderTest {
    static ResourceLoader localResourceLoader = new LocalResourceLoader();
    static DirectoryBasedSchemaProvider provider;

    @BeforeAll
    static void before() {
        provider = new DirectoryBasedSchemaProvider(JsonSchemaType.V2019_09, localResourceLoader);
    }

    @Test
    void schemaLoadedFromLocalFileSystemOkTest() {
        Path directory = getBaseDirectoryFor("DirectoryBasedSchemaProviderTest");

        String pathToSchema = Paths.get(directory.toString(), "simple.schema.json").toString();

        DirectorySchemaPointer pointer = DirectorySchemaPointer.createFromString(pathToSchema);

        NetworkntSchema schema = provider.loadSchema(pointer);

        assertNotNull(schema);
        assertNotNull(schema.getSchema());
    }

    private Path getBaseDirectoryFor(String... additional) {
        String pathToFile = Thread.currentThread().getContextClassLoader().getResource("test.file.txt").getPath();
        String base = pathToFile.substring(0, pathToFile.lastIndexOf("/"));
        return Paths.get(base, additional);
    }

    @Test
    void loadedSchemaContainsAJsonSchemaDataTest() {
        Path baseDirectory = getBaseDirectoryFor("DirectoryBasedSchemaProviderTest");
        String pathToFile = Paths.get(baseDirectory.toString(), "simple.schema.json").toString();
        DirectorySchemaPointer schemaPointer = DirectorySchemaPointer.createFromString(pathToFile);

        NetworkntSchema schema = provider.loadSchema(schemaPointer);

        assertEquals("an_id_for_simple_schema_json", schema.getSchema().getSchemaNode().get("$id").textValue());
    }

    @Test
    void loadsSchemaWithRefsToAnotherSchemaInLocalFileSystemOkTest() throws IOException {
        Path baseDirectory = getBaseDirectoryFor("DirectoryBasedSchemaProviderTest");
        String pathToFile = Paths.get(baseDirectory.toString(), "complex.schema.json").toString();
        DirectorySchemaPointer schemaPointer = DirectorySchemaPointer.createFromString(pathToFile);

        NetworkntSchema schema = provider.loadSchema(schemaPointer);

        assertEquals("an_id_for_complex_schema_json", schema.getSchema().getSchemaNode().get("$id").textValue());

        ObjectMapper mapper = new ObjectMapper();

        JsonNode jsonNode = mapper.readValue("{\"field1\":\"sss\", \"field2\":33.2, \"field3\":23}", JsonNode.class);
        Set<ValidationMessage> violations = schema.getSchema().validate(jsonNode);

        Assertions.assertFalse(violations.isEmpty());
    }
}
