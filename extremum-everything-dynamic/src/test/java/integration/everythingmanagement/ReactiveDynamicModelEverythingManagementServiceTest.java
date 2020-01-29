package integration.everythingmanagement;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import integration.SpringBootTestWithServices;
import io.atlassian.fugue.Try;
import io.extremum.common.exceptions.ModelNotFoundException;
import io.extremum.dynamic.DynamicModuleAutoConfiguration;
import io.extremum.dynamic.ReactiveDescriptorDeterminator;
import io.extremum.dynamic.SchemaMetaService;
import io.extremum.dynamic.everything.dto.JsonDynamicModelResponseDto;
import io.extremum.dynamic.everything.management.HybridEverythingManagementService;
import io.extremum.dynamic.everything.management.ReactiveDynamicModelEverythingManagementService;
import io.extremum.dynamic.metadata.impl.DefaultJsonDynamicModelMetadataProvider;
import io.extremum.dynamic.models.impl.JsonDynamicModel;
import io.extremum.dynamic.services.JsonBasedDynamicModelService;
import io.extremum.dynamic.validator.ValidationContext;
import io.extremum.dynamic.validator.services.impl.JsonDynamicModelValidator;
import io.extremum.dynamic.watch.DynamicModelWatchService;
import io.extremum.everything.reactive.config.ReactiveEverythingConfiguration;
import io.extremum.security.DataSecurity;
import io.extremum.security.PrincipalSource;
import io.extremum.security.RoleSecurity;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.ResponseDto;
import io.extremum.starter.CommonConfiguration;
import io.extremum.watch.config.WatchConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.stubbing.answers.ReturnsArgumentAt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.Map;

import static io.extremum.sharedmodels.basic.Model.FIELDS.*;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static reactor.core.publisher.Mono.empty;
import static reactor.core.publisher.Mono.just;

@ActiveProfiles("everything-test")
@SpringBootTest(classes = {
        WatchConfiguration.class,
        CommonConfiguration.class,
        ReactiveEverythingConfiguration.class,
        DynamicModuleAutoConfiguration.class
})
@MockBeans({
        @MockBean(RoleSecurity.class),
        @MockBean(DataSecurity.class),
        @MockBean(PrincipalSource.class)
})
public class ReactiveDynamicModelEverythingManagementServiceTest extends SpringBootTestWithServices {
    private static final Descriptor NOT_EXISTENT_DESCRIPTOR = Descriptor.builder()
            .internalId("000000000000000000000000")
            .externalId("00000000-0000-0000-0000-000000000000")
            .build();

    @Autowired
    HybridEverythingManagementService hybridEverythingManagementService;

    @Autowired
    JsonBasedDynamicModelService dynamicModelService;

    @Autowired
    ReactiveDescriptorDeterminator reactiveDescriptorDeterminator;

    @MockBean
    SchemaMetaService schemaMetaService;

    @MockBean
    JsonDynamicModelValidator jsonDynamicModelValidator;

    @MockBean
    DefaultJsonDynamicModelMetadataProvider metadataProvider;

    @MockBean
    DynamicModelWatchService watchService;

    ReactiveDynamicModelEverythingManagementService dynamicModelEverythingManagementService;

    @Autowired
    private ObjectMapper mapper;

    @BeforeEach
    void beforeEach() {
        doReturn(just(Try.successful(mock(ValidationContext.class))))
                .when(jsonDynamicModelValidator).validate(any());

        doAnswer(new ReturnsArgumentAt(0))
                .when(metadataProvider).provideMetadata(any());

        dynamicModelEverythingManagementService =
                hybridEverythingManagementService.getDynamicModelEverythingManagementService();

        when(watchService.registerSaveOperation(any())).thenReturn(empty());
        when(watchService.registerPatchOperation(any(), any())).thenReturn(empty());
        when(watchService.registerDeleteOperation(any())).thenReturn(empty());
    }

    @Test
    void getOperation_shouldReturn_model() throws IOException {
        JsonDynamicModel model = createModel("TestDynamicModel", "{\"a\":\"b\"}");
        JsonDynamicModel savedModel = dynamicModelService.saveModel(model).block();

        ResponseDto found = dynamicModelEverythingManagementService.get(savedModel.getId(), false).block();

        assertNotNull(found);
        assertEquals(savedModel.getId(), found.getId());
        assertEquals(model.getModelName(), found.getModel());
        assertTrue(((JsonDynamicModelResponseDto) found).getData().containsKey("a"));
        assertEquals("b", ((JsonDynamicModelResponseDto) found).getData().get("a"));

        String serialized = serialize(found);
        JsonNode deserialized = deserializeToNode(serialized);

        checkServiceFields(deserialized);
    }

    private void checkServiceFields(JsonNode deserialized) {
        assertTrue(deserialized.has(created.name()));
        assertTrue(deserialized.has(model.name()));
        assertTrue(deserialized.has(version.name()));
        assertTrue(deserialized.has(model.name()));
    }

    private JsonNode deserializeToNode(String serialized) throws IOException {
        return mapper.readValue(serialized, JsonNode.class);
    }

    private String serialize(ResponseDto found) throws JsonProcessingException {
        return mapper.writeValueAsString(found);
    }

    @Test
    void getOperation_shouldReturn_NotFoundException_if_modelIsNotExists() {
        Mono<ResponseDto> result = dynamicModelEverythingManagementService.get(NOT_EXISTENT_DESCRIPTOR, false);

        StepVerifier.create(result)
                .expectError(ModelNotFoundException.class)
                .verify();
    }

    @Test
    void patchOperation_shouldPerformPatching_andReturnAPatchedModel() throws IOException {
        String modelName = "PatchingDynamicModel";

        doReturn(modelName).when(schemaMetaService).getSchemaNameByModel(modelName);

        JsonDynamicModel patchingModel = createModel(modelName, "{\"a\":\"b\"}");
        JsonDynamicModel saved = dynamicModelService.saveModel(patchingModel).block();

        JsonNode nodePatch = createJsonNodeForString("[{\"op\":\"replace\", \"path\":\"/a\", \"value\":\"c\"}]");
        JsonPatch patch = JsonPatch.fromJson(nodePatch);
        Mono<ResponseDto> result = dynamicModelEverythingManagementService.patch(saved.getId(), patch, false);

        StepVerifier.create(result)
                .assertNext(patched -> {
                    assertEquals(saved.getId(), patched.getId());
                    assertEquals(patchingModel.getModelName(), patched.getModel());
                    assertEquals("c", ((JsonDynamicModelResponseDto) patched).getData().get("a"));
                }).verifyComplete();

        Mono<JsonDynamicModel> foundPatchedModel = dynamicModelService.findById(saved.getId());
        StepVerifier.create(foundPatchedModel)
                .assertNext(patched -> {
                    assertEquals(saved.getId(), patched.getId());
                    assertEquals(patchingModel.getModelName(), patched.getModelName());
                    assertEquals("c", patched.getModelData().get("a"));
                }).verifyComplete();

        verify(watchService, times(1)).registerPatchOperation(any(), any());
    }

    @Test
    void patchOperation_shouldReturnsWithNotFoundException_if_modelIsNotExists() throws IOException {
        JsonNode nodePatch = createJsonNodeForString("[{\"op\":\"replace\", \"path\":\"/a\", \"value\":\"c\"}]");
        JsonPatch patch = JsonPatch.fromJson(nodePatch);
        Mono<ResponseDto> result = dynamicModelEverythingManagementService.patch(NOT_EXISTENT_DESCRIPTOR, patch, false);

        StepVerifier.create(result)
                .expectError(ModelNotFoundException.class)
                .verify();

        verify(watchService, never()).registerPatchOperation(any(), any());
    }

    @Test
    void removeOperation_shouldRemoveModel_andReturnsWithEmptyPipe() {
        JsonDynamicModel model = createModel("ModelForRemove", "{\"a\":\"b\"}");
        JsonDynamicModel savedModel = dynamicModelService.saveModel(model).block();

        Mono<Void> result = dynamicModelEverythingManagementService.remove(savedModel.getId());

        StepVerifier.create(result).verifyComplete();

        verify(watchService, times(1)).registerDeleteOperation(any());
    }

    @Test
    void removeOperation_shouldReturnsWithEmptyPipe_if_modelIsNotExists() {
        Mono<Void> result = dynamicModelEverythingManagementService.remove(NOT_EXISTENT_DESCRIPTOR);

        StepVerifier.create(result).verifyComplete();

        verify(watchService, never()).registerDeleteOperation(any());
    }

    private JsonDynamicModel createModel(String modelName, String stringModelData) {
        JsonNode modelData = createJsonNodeForString(stringModelData);
        return new JsonDynamicModel(modelName, convertToMap(modelData));
    }

    private Map<String, Object> convertToMap(JsonNode modelData) {
        return new ObjectMapper().convertValue(modelData, new TypeReference<Map<String, Object>>() {
        });
    }

    private JsonNode createJsonNodeForString(String stringModelData) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(stringModelData, JsonNode.class);
        } catch (IOException e) {
            String msg = format("Unable to create JsonNode from source %s: %s", stringModelData, e);
            fail(msg);
            throw new RuntimeException(msg, e);
        }
    }
}
