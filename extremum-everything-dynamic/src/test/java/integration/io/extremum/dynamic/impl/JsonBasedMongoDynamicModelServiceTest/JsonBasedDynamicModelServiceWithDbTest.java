package integration.io.extremum.dynamic.impl.JsonBasedMongoDynamicModelServiceTest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import integration.SpringBootTestWithServices;
import io.extremum.common.exceptions.ModelNotFoundException;
import io.extremum.dynamic.GithubSchemaProperties;
import io.extremum.dynamic.metadata.impl.DefaultJsonDynamicModelMetadataProvider;
import io.extremum.dynamic.models.impl.JsonDynamicModel;
import io.extremum.dynamic.schema.JsonSchemaType;
import io.extremum.dynamic.schema.provider.networknt.NetworkntSchemaProvider;
import io.extremum.dynamic.schema.provider.networknt.caching.NetworkntCacheManager;
import io.extremum.dynamic.schema.provider.networknt.impl.FileSystemNetworkntSchemaProvider;
import io.extremum.dynamic.services.impl.JsonBasedDynamicModelService;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
@ActiveProfiles("save-model-test")
class JsonBasedDynamicModelServiceWithDbTest extends SpringBootTestWithServices {
    @Autowired
    JsonBasedDynamicModelService service;

    @Autowired
    GithubSchemaProperties githubSchemaProperties;

    @Autowired
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
                Paths.get(base, "schemas/")
        );

        networkntCacheManager.cacheSchema(provider.loadSchema(schemaName), schemaName);

        String modelName = "complex.schema.json";
        JsonNode modelData = toJsonNode("{\"field1\":\"aaa\", \"field3\":{\"externalField\":\"bbb\"}}");

        JsonDynamicModel model = new JsonDynamicModel(modelName, modelData);

        Mono<JsonDynamicModel> saved = service.saveModel(model);

        StepVerifier.setDefaultTimeout(Duration.of(30, ChronoUnit.SECONDS));

        StepVerifier.create(saved)
                .expectNextMatches(m ->
                        m.getModelName().equals(model.getModelName()) &&
                                m.getModelData().toString().equals(model.getModelData().toString()) &&
                                m.getId() != null &&
                                model.getModelName().equals(m.getId().getModelType())
                )
                .verifyComplete();
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
        JsonNode modelData = toJsonNode("{\"field1\":\"aaa\", \"field3\":{\"externalField\":\"bbb\"}}");

        JsonDynamicModel model = new JsonDynamicModel(modelName, modelData);

        JsonDynamicModel saved = service.saveModel(model).block();

        JsonDynamicModel found = service.findById(saved.getId()).block();

        assertNotNull(found);
        assertNotNull(found.getId());
        assertNotNull(model.getModelName(), found.getId().getModelType());
        assertEquals(saved.getId(), found.getId());
        assertEquals(model.getModelName(), found.getModelName());
        assertEquals(model.getModelData(), found.getModelData());
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

        networkntCacheManager.cacheSchema(provider.loadSchema(schemaName), schemaName);

        String modelName = "complex.schema.json";
        JsonNode modelData = toJsonNode("{\"field1\":\"aaa\", \"field3\":{\"externalField\":\"bbb\"}}");

        JsonDynamicModel model = new JsonDynamicModel(modelName, modelData);

        JsonDynamicModel saved = service.saveModel(model).block();

        JsonDynamicModel found = service.findById(saved.getId()).block();

        Descriptor idOfTheFoundModel = found.getId();
        assertEquals(saved.getId(), idOfTheFoundModel);

        JsonNode modelData_updated = toJsonNode("{\"field1\":\"bbb\", \"field3\":{\"externalField\":\"bbb\"}}");
        JsonDynamicModel updatedModel = new JsonDynamicModel(idOfTheFoundModel, found.getModelName(), modelData_updated);
        JsonDynamicModel updatedResult = service.saveModel(updatedModel).block();

        assertEquals(idOfTheFoundModel, updatedResult.getId());

        JsonDynamicModel foundUpdated = service.findById(idOfTheFoundModel).block();

        assertEquals("bbb", foundUpdated.getModelData().get("field1").textValue());
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
        JsonNode modelData = toJsonNode("{\"field1\":\"aaa\", \"field3\":{\"externalField\":\"bbb\"}}");

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
    void findByIdReturnsException_when_modelNotFound_in_nonExistentCollection() throws IOException {
        Descriptor mockDescriptor = mock(Descriptor.class);
        when(mockDescriptor.getModelTypeReactively()).thenReturn(Mono.just("non-model-type"));
        when(mockDescriptor.getInternalId()).thenReturn(ObjectId.get().toString());

        Mono<JsonDynamicModel> result = service.findById(mockDescriptor);

        StepVerifier.create(result)
                .expectError(ModelNotFoundException.class)
                .verify();
    }

    private JsonNode toJsonNode(String stringData) throws IOException {
        return new ObjectMapper().readValue(stringData, JsonNode.class);
    }
}
