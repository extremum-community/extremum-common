package integration.everythingmanagement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import integration.SpringBootTestWithServices;
import io.atlassian.fugue.Either;
import io.extremum.common.exceptions.ModelNotFoundException;
import io.extremum.dynamic.DynamicModuleAutoConfiguration;
import io.extremum.dynamic.ReactiveDescriptorDeterminator;
import io.extremum.dynamic.everything.dto.JsonDynamicModelResponseDto;
import io.extremum.dynamic.everything.management.HybridEverythingManagementService;
import io.extremum.dynamic.everything.management.ReactiveDynamicModelEverythingManagementService;
import io.extremum.dynamic.metadata.impl.DefaultJsonDynamicModelMetadataProvider;
import io.extremum.dynamic.models.impl.JsonDynamicModel;
import io.extremum.dynamic.services.impl.JsonBasedDynamicModelService;
import io.extremum.dynamic.validator.ValidationContext;
import io.extremum.dynamic.validator.services.impl.JsonDynamicModelValidator;
import io.extremum.everything.reactive.config.ReactiveEverythingConfiguration;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.ResponseDto;
import io.extremum.starter.CommonConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.stubbing.answers.ReturnsArgumentAt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static reactor.core.publisher.Mono.just;

@ActiveProfiles("everything-test")
@SpringBootTest(classes = {
        CommonConfiguration.class,
        ReactiveEverythingConfiguration.class,
        DynamicModuleAutoConfiguration.class
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
    JsonDynamicModelValidator jsonDynamicModelValidator;

    @MockBean
    DefaultJsonDynamicModelMetadataProvider metadataProvider;

    ReactiveDynamicModelEverythingManagementService dynamicModelEverythingManagementService;

    @BeforeEach
    void beforeEach() {
        doReturn(just(Either.right(mock(ValidationContext.class))))
                .when(jsonDynamicModelValidator).validate(any());

        doAnswer(new ReturnsArgumentAt(0))
                .when(metadataProvider).provideMetadata(any());

        dynamicModelEverythingManagementService =
                hybridEverythingManagementService.getDynamicModelEverythingManagementService();
    }

    @Test
    void getOperation_shouldReturn_model() {
        JsonDynamicModel model = createModel("TestDynamicModel", "{\"a\":\"b\"}");
        JsonDynamicModel savedModel = dynamicModelService.saveModel(model).block();

        ResponseDto found = dynamicModelEverythingManagementService.get(savedModel.getId(), false).block();

        assertNotNull(found);
        assertEquals(savedModel.getId(), found.getId());
        assertEquals(model.getModelName(), found.getModel());
        assertEquals(model.getModelData().toString(), ((JsonDynamicModelResponseDto) found).getData().toString());
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

        reactiveDescriptorDeterminator.registerModelName(modelName);

        JsonDynamicModel patchingModel = createModel(modelName, "{\"a\":\"b\"}");
        JsonDynamicModel saved = dynamicModelService.saveModel(patchingModel).block();

        JsonNode nodePatch = createJsonNodeForString("[{\"op\":\"replace\", \"path\":\"/a\", \"value\":\"c\"}]");
        JsonPatch patch = JsonPatch.fromJson(nodePatch);
        Mono<ResponseDto> result = dynamicModelEverythingManagementService.patch(saved.getId(), patch, false);

        StepVerifier.create(result)
                .assertNext(patched -> {
                    assertEquals(saved.getId(), patched.getId());
                    assertEquals(patchingModel.getModelName(), patched.getModel());
                    assertEquals("c", ((JsonDynamicModelResponseDto) patched).getData().get("a").textValue());
                }).verifyComplete();

        Mono<JsonDynamicModel> foundPatchedModel = dynamicModelService.findById(saved.getId());
        StepVerifier.create(foundPatchedModel)
                .assertNext(patched -> {
                    assertEquals(saved.getId(), patched.getId());
                    assertEquals(patchingModel.getModelName(), patched.getModelName());
                    assertEquals("c", patched.getModelData().get("a").textValue());
                }).verifyComplete();
    }

    @Test
    void patchOperation_shouldReturnsWithNotFoundException_if_modelIsNotExists() throws IOException {
        JsonNode nodePatch = createJsonNodeForString("[{\"op\":\"replace\", \"path\":\"/a\", \"value\":\"c\"}]");
        JsonPatch patch = JsonPatch.fromJson(nodePatch);
        Mono<ResponseDto> result = dynamicModelEverythingManagementService.patch(NOT_EXISTENT_DESCRIPTOR, patch, false);

        StepVerifier.create(result)
                .expectError(ModelNotFoundException.class)
                .verify();
    }

    @Test
    void removeOperation_shouldRemoveModel_andReturnsWithEmptyPipe() {
        JsonDynamicModel model = createModel("ModelForRemove", "{\"a\":\"b\"}");
        JsonDynamicModel savedModel = dynamicModelService.saveModel(model).block();

        Mono<Void> result = dynamicModelEverythingManagementService.remove(savedModel.getId());

        StepVerifier.create(result).verifyComplete();
    }

    @Test
    void removeOperation_shouldReturnsWithEmptyPipe_if_modelIsNotExists() {
        Mono<Void> result = dynamicModelEverythingManagementService.remove(NOT_EXISTENT_DESCRIPTOR);

        StepVerifier.create(result).verifyComplete();
    }

    private JsonDynamicModel createModel(String modelName, String stringModelData) {
        JsonNode modelData = createJsonNodeForString(stringModelData);
        return new JsonDynamicModel(modelName, modelData);
    }

    private JsonNode createJsonNodeForString(String stringModelData) {
        try {
            return new ObjectMapper().readValue(stringModelData, JsonNode.class);
        } catch (IOException e) {
            String msg = format("Unable to create JsonNode from source %s: %s", stringModelData, e);
            fail(msg);
            throw new RuntimeException(msg, e);
        }
    }
}
