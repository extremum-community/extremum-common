package integration.io.extremum.dynamic.impl.JsonBasedMongoDynamicModelServiceTest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import integration.SpringBootTestWithServices;
import io.extremum.common.exceptions.ModelNotFoundException;
import io.extremum.dynamic.DynamicModuleAutoConfiguration;
import io.extremum.dynamic.GithubSchemaProperties;
import io.extremum.dynamic.metadata.impl.DefaultJsonDynamicModelMetadataProvider;
import io.extremum.dynamic.models.impl.JsonDynamicModel;
import io.extremum.dynamic.schema.JsonSchemaType;
import io.extremum.dynamic.schema.networknt.NetworkntSchema;
import io.extremum.dynamic.schema.provider.networknt.NetworkntSchemaProvider;
import io.extremum.dynamic.schema.provider.networknt.caching.NetworkntCacheManager;
import io.extremum.dynamic.schema.provider.networknt.impl.FileSystemNetworkntSchemaProvider;
import io.extremum.dynamic.services.impl.JsonBasedDynamicModelService;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.starter.CommonConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static io.extremum.common.utils.DateUtils.parseZonedDateTimeFromISO_8601;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Slf4j
@ActiveProfiles("save-model-test")
@ContextConfiguration(classes = {CommonConfiguration.class, DynamicModuleAutoConfiguration.class})
class JsonBasedDynamicModelServiceWithDbTest extends SpringBootTestWithServices {
    @Autowired
    JsonBasedDynamicModelService service;

    @Autowired
    GithubSchemaProperties githubSchemaProperties;


    @Autowired
    ReactiveMongoOperations operations;

    @Autowired
    private ObjectMapper mapper;

    @SpyBean
    NetworkntCacheManager networkntCacheManager;

    @MockBean
    DefaultJsonDynamicModelMetadataProvider metadataProvider;

    @Test
    void validModelSavedInMongo() throws IOException {
        String schemaName = "complex.schema.json";

        String pathToFile = this.getClass().getClassLoader().getResource("test.file.txt").getPath();
        String base = pathToFile.substring(0, pathToFile.lastIndexOf("/"));

        NetworkntSchemaProvider provider = new FileSystemNetworkntSchemaProvider(
                JsonSchemaType.V2019_09,
                Paths.get(base, "schemas")
        );

        networkntCacheManager.cacheSchema(provider.loadSchema(schemaName), schemaName);

        String modelName = "complex.schema.json";
        Map<String, Object> modelData = toMap("{\n" +
                "  \"field1\": \"aaa\",\n" +
                "  \"field3\": {\n" +
                "    \"externalField\": \"bbb\"\n" +
                "  " +
                "},\n" +
                "  \"fieldObject\": {\n" +
                "    \"fieldDate1\": \"2013-01-09T09:31:26.111111-0500\",\n" +
                "    \"fieldDate2\": \"2014-01-09T09:31:26.111111-0500\"\n" +
                "  }\n" +
                "}");

        JsonDynamicModel model = new JsonDynamicModel(modelName, modelData);

        Mono<JsonDynamicModel> saved = service.saveModel(model);

        StepVerifier.setDefaultTimeout(Duration.of(30, ChronoUnit.SECONDS));

        StepVerifier.create(saved)
                .assertNext(m -> {
                            assertEquals(model.getModelName(), m.getModelName());

                            assertEquals(model.getModelData().get("field1").toString(), m.getModelData().get("field1").toString());

                            Map<String, Object> map = ((Map) m.getModelData().get("field3"));

                            assertTrue(map.containsKey("externalField"));
                            assertEquals(((Map) model.getModelData().get("field3")).get("externalField"), map.get("externalField"));

                            assertNotNull(m.getId());
                            assertEquals(model.getModelName(), m.getId().getModelType());
                        }
                )
                .verifyComplete();

        for (String dateField : Arrays.asList("fieldObject.fieldDate1", "fieldObject.fieldDate2")) {
            Iterable<Document> documents = Flux.from(operations.getCollection("complex_schema_json")
                    .find(new Document(dateField, new Document("$type", "date")))
                    .limit(2)).toIterable();

            assertEquals(1, StreamSupport.stream(documents.spliterator(), false).count());
        }
    }

    @Test
    void getModelById() throws IOException {
        when(metadataProvider.provideMetadata(any())).thenAnswer((Answer<JsonDynamicModel>) invocation -> {
            Object[] args = invocation.getArguments();
            return (JsonDynamicModel) args[0];
        });

        String schemaName = "complex.schema.json";

        String pathToFile = this.getClass().getClassLoader().getResource("test.file.txt").getPath();
        String base = pathToFile.substring(0, pathToFile.lastIndexOf("/"));

        NetworkntSchemaProvider provider = new FileSystemNetworkntSchemaProvider(
                JsonSchemaType.V2019_09,
                Paths.get(base, "schemas/")
        );

        networkntCacheManager.cacheSchema(provider.loadSchema(schemaName), schemaName);

        String modelName = "complex.schema.json";
        Map<String, Object> modelData = toMap("{\"field1\":\"aaa\", \"field3\":{\"externalField\":\"bbb\"}}");

        JsonDynamicModel model = new JsonDynamicModel(modelName, modelData);

        JsonDynamicModel saved = service.saveModel(model).block();

        JsonDynamicModel found = service.findById(saved.getId()).block();

        assertNotNull(found);
        assertNotNull(found.getId());
        assertNotNull(model.getModelName(), found.getId().getModelType());
        assertEquals(saved.getId(), found.getId());
        assertEquals(model.getModelName(), found.getModelName());
        assertEquals(model.getModelData().get("field1"), found.getModelData().get("field1"));
        assertEquals(model.getModelData().get("field3"), found.getModelData().get("field3"));
    }

    @Test
    void updateModelTest() throws IOException {
        when(metadataProvider.provideMetadata(any())).thenAnswer((Answer<JsonDynamicModel>) invocation -> {
            Object[] args = invocation.getArguments();
            return (JsonDynamicModel) args[0];
        });

        String schemaName = "complex.schema.json";

        String pathToFile = this.getClass().getClassLoader().getResource("test.file.txt").getPath();
        String base = pathToFile.substring(0, pathToFile.lastIndexOf("/"));

        NetworkntSchemaProvider provider = new FileSystemNetworkntSchemaProvider(
                JsonSchemaType.V2019_09,
                Paths.get(base, "schemas/")
        );

        String modelName = "complexModel";

        networkntCacheManager.cacheSchema(provider.loadSchema(schemaName), modelName);

        Map<String, Object> modelData = toMap("{\"field1\":\"aaa\", \"field3\":{\"externalField\":\"bbb\"}}");

        JsonDynamicModel model = new JsonDynamicModel(modelName, modelData);

        JsonDynamicModel saved = service.saveModel(model).block();

        JsonDynamicModel found = service.findById(saved.getId()).block();

        Descriptor idOfTheFoundModel = found.getId();
        assertEquals(saved.getId(), idOfTheFoundModel);

        Map<String, Object> modelData_updated = toMap("{\"field1\":\"bbb\", \"field3\":{\"externalField\":\"bbb\"}, \n" +
                "\"created\":\"blablabla\",\n" +
                "\"modified\": \"blababla\",\n" +
                "\"model\": \"complexModel\",\n" +
                "\"version\": 1}");
        JsonDynamicModel updatedModel = new JsonDynamicModel(idOfTheFoundModel, found.getModelName(), modelData_updated);
        JsonDynamicModel updatedResult = service.saveModel(updatedModel).block();

        assertEquals(idOfTheFoundModel, updatedResult.getId());

        JsonDynamicModel foundUpdated = service.findById(idOfTheFoundModel).block();

        assertEquals("bbb", foundUpdated.getModelData().get("field1"));
    }

    // negative tests

    @Test
    void findByIdReturnsException_when_modelNotFound_in_existingCollection() throws IOException {
        String schemaName = "complex.schema.json";

        String pathToFile = this.getClass().getClassLoader().getResource("test.file.txt").getPath();
        String base = pathToFile.substring(0, pathToFile.lastIndexOf("/"));

        NetworkntSchemaProvider provider = new FileSystemNetworkntSchemaProvider(
                JsonSchemaType.V2019_09,
                Paths.get(base, "schemas/")
        );

        networkntCacheManager.cacheSchema(provider.loadSchema(schemaName), schemaName);

        String modelName = "complex.schema.json";
        Map<String, Object> modelData = toMap("{\"field1\":\"aaa\", \"field3\":{\"externalField\":\"bbb\"}}");

        JsonDynamicModel model = new JsonDynamicModel(modelName, modelData);

        service.saveModel(model).block();

        Descriptor mockDescriptor = mock(Descriptor.class);
        when(mockDescriptor.getModelTypeReactively()).thenReturn(Mono.just("complex.schema.json"));
        when(mockDescriptor.getInternalId()).thenReturn(ObjectId.get().toString());

        Mono<JsonDynamicModel> result = service.findById(mockDescriptor);

        StepVerifier.create(result)
                .expectError(ModelNotFoundException.class)
                .verify();
    }

    @Test
    void findByIdReturnsException_when_modelNotFound_in_nonExistentCollection() {
        Descriptor mockDescriptor = mock(Descriptor.class);
        when(mockDescriptor.getModelTypeReactively()).thenReturn(Mono.just("non-model-type"));
        when(mockDescriptor.getInternalId()).thenReturn(ObjectId.get().toString());

        Mono<JsonDynamicModel> result = service.findById(mockDescriptor);

        StepVerifier.create(result)
                .expectError(ModelNotFoundException.class)
                .verify();
    }

    @Test
    void modelSaved_withFields_create_modified_version() throws IOException {
        NetworkntSchema networkntSchemaMock = mock(NetworkntSchema.class);
        JsonSchema jsonSchemaMock = mock(JsonSchema.class);

        doReturn(Collections.emptySet())
                .when(jsonSchemaMock).validate(any(), any());

        doReturn(jsonSchemaMock)
                .when(networkntSchemaMock).getSchema();

        doReturn(Optional.of(networkntSchemaMock))
                .when(networkntCacheManager).fetchFromCache(anyString());

        Map<String, Object> data = toMap("{\"a\":  \"b\"}");

        JsonDynamicModel model = new JsonDynamicModel("modelName", data);

        JsonDynamicModel saved = service.saveModel(model).block();

        String created = (String) saved.getModelData().get(Model.FIELDS.created.name());
        String modified = (String) saved.getModelData().get(Model.FIELDS.model.name());

        assertNotNull(created);
        assertNotNull(modified);
        assertNotNull(saved.getModelData().get(Model.FIELDS.version.name()));

        assertDoesNotThrow(() -> parseZonedDateTimeFromISO_8601(created));
        assertDoesNotThrow(() -> parseZonedDateTimeFromISO_8601(modified));

        assertEquals("modelName", saved.getModelData().get(Model.FIELDS.model.name()));
        assertEquals(1L, (long) saved.getModelData().get(Model.FIELDS.version.name()));
    }

    @Test
    void modelUpdated_withFields_modified_changed__version_incremented() throws IOException {
        NetworkntSchema networkntSchemaMock = mock(NetworkntSchema.class);
        JsonSchema jsonSchemaMock = mock(JsonSchema.class);

        doReturn(Collections.emptySet())
                .when(jsonSchemaMock).validate(any(), any());

        doReturn(jsonSchemaMock)
                .when(networkntSchemaMock).getSchema();

        doReturn(Optional.of(networkntSchemaMock))
                .when(networkntCacheManager).fetchFromCache(anyString());

        Map<String, Object> data = toMap("{\"a\":  \"b\"}");

        JsonDynamicModel model = new JsonDynamicModel("modelName", data);

        JsonDynamicModel saved = service.saveModel(model).block();


        String created = (String) saved.getModelData().get(Model.FIELDS.created.name());
        String modifiedWhenCreated = (String) saved.getModelData().get(Model.FIELDS.modified.name());

        assertNotNull(created);
        assertNotNull(modifiedWhenCreated);
        assertNotNull(saved.getModelData().get(Model.FIELDS.version.name()));

        assertDoesNotThrow(() -> parseZonedDateTimeFromISO_8601(created));
        assertDoesNotThrow(() -> parseZonedDateTimeFromISO_8601(modifiedWhenCreated));
        assertEquals("modelName", saved.getModelData().get(Model.FIELDS.model.name()));

        JsonDynamicModel updated = service.saveModel(saved).block();

        assertEquals(2, updated.getModelData().get(Model.FIELDS.version.name()));

        String modifiedWhenUpdated = (String) updated.getModelData().get(Model.FIELDS.modified.name());
        assertTrue(
                parseZonedDateTimeFromISO_8601(modifiedWhenCreated).isBefore(
                        parseZonedDateTimeFromISO_8601(modifiedWhenUpdated)
                )
        );
    }


    private Map<String, Object> toMap(@Language("JSON") String stringData) throws IOException {
        JsonNode node = mapper.readValue(stringData, JsonNode.class);
        return mapper.convertValue(node, new TypeReference<Map<String, Object>>() {
        });
    }
}
