package integration.io.extremum.dynamic.impl.JsonBasedMongoDynamicModelServiceTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import io.extremum.dynamic.dao.MongoDynamicModelDao;
import io.extremum.dynamic.metadata.impl.DefaultJsonDynamicModelMetadataProvider;
import io.extremum.dynamic.models.impl.JsonDynamicModel;
import io.extremum.dynamic.schema.networknt.NetworkntSchema;
import io.extremum.dynamic.schema.provider.networknt.impl.FileSystemNetworkntSchemaProvider;
import io.extremum.dynamic.services.DateTypesNormalizer;
import io.extremum.dynamic.services.DatesProcessor;
import io.extremum.dynamic.services.impl.JsonBasedDynamicModelService;
import io.extremum.dynamic.validator.ValidationContext;
import io.extremum.dynamic.validator.exceptions.DynamicModelValidationException;
import io.extremum.dynamic.validator.exceptions.SchemaNotFoundException;
import io.extremum.dynamic.validator.services.impl.networknt.NetworkntJsonDynamicModelValidator;
import io.extremum.sharedmodels.basic.Model;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import static io.atlassian.fugue.Try.successful;
import static io.extremum.dynamic.TestUtils.loadResourceAsInputStream;
import static io.extremum.sharedmodels.basic.Model.FIELDS.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static reactor.core.publisher.Mono.just;

@SpringBootTest(classes = JsonBasedDynamicModelServiceTestConfiguration.class)
class JsonBasedDynamicModelServiceTest {
    @MockBean
    FileSystemNetworkntSchemaProvider schemaProvider;

    @SpyBean
    NetworkntJsonDynamicModelValidator modelValidator;

    @Autowired
    DateTypesNormalizer normalizer;

    @Autowired
    DatesProcessor datesProcessor;

    @Autowired
    ObjectMapper mapper;

    @Mock
    MongoDynamicModelDao dao;

    @Mock
    DefaultJsonDynamicModelMetadataProvider metadataProvider;

    @Captor
    ArgumentCaptor<JsonDynamicModel> modelCaptor;

    @Test
    void saveModel() {
        final JsonDynamicModel model = mock(JsonDynamicModel.class);

        NetworkntSchema schema = mock(NetworkntSchema.class);

        String schemaName = "test_schema";

        when(schemaProvider.loadSchema(schemaName)).thenReturn(schema);
        when(model.getModelData()).thenReturn(new HashMap<>());
        when(model.getModelName()).thenReturn(schemaName);

        doReturn(just(successful(mock(ValidationContext.class)))).when(modelValidator).validate(any());

        when(dao.create(any(), anyString())).thenReturn(just(model));

        JsonBasedDynamicModelService service = new JsonBasedDynamicModelService(dao, modelValidator, metadataProvider,
                normalizer, datesProcessor);

        Mono<JsonDynamicModel> saved = service.saveModel(model);

        StepVerifier.setDefaultTimeout(Duration.of(30, ChronoUnit.SECONDS));

        StepVerifier.create(saved)
                .assertNext(resultModel -> {
                    assertEquals(resultModel.getId(), model.getId());
                    assertEquals(resultModel.getModelName(), model.getModelName());
                })
                .verifyComplete();
        verify_Validator_HasAccept_Model_1_times(model);
        verify_Normalizer_HasAccept_Model_1_times();
        verify_DynamicModelDao_HasAccept_Model_1_times(model);
    }

    @Test
    void notValidModelIsNotSaves() {
        JsonBasedDynamicModelService service = new JsonBasedDynamicModelService(dao, modelValidator, metadataProvider,
                normalizer, datesProcessor);

        Map<String, Object> invalidModelRawValue = new HashMap<>();
        invalidModelRawValue.put("field1", 1);

        JsonDynamicModel model = new JsonDynamicModel("model", invalidModelRawValue);

        JsonSchema schema = JsonSchemaFactory.getInstance()
                .getSchema(loadResourceAsInputStream(this.getClass().getClassLoader(), "schemas/simple.schema.json"));

        when(schemaProvider.loadSchema(any())).thenReturn(new NetworkntSchema(schema));

        Mono<JsonDynamicModel> result = service.saveModel(model);

        verify(dao, never()).create(any(), anyString());

        StepVerifier.setDefaultTimeout(Duration.of(30, ChronoUnit.SECONDS));

        StepVerifier.create(result)
                .expectError(DynamicModelValidationException.class)
                .verify();
    }

    @Test
    void modelIsNotSaved_schemaDoesntExists() {
        JsonBasedDynamicModelService service = new JsonBasedDynamicModelService(dao, modelValidator, metadataProvider,
                normalizer, datesProcessor);

        String unknownModel = "unknownModel";

        JsonDynamicModel jModel = new JsonDynamicModel(unknownModel, null);

        when(schemaProvider.loadSchema(unknownModel)).thenThrow(SchemaNotFoundException.class);

        Mono<JsonDynamicModel> result = service.saveModel(jModel);

        verify(dao, never()).create(any(), anyString());

        StepVerifier.setDefaultTimeout(Duration.of(30, ChronoUnit.SECONDS));

        StepVerifier.create(result)
                .expectError(SchemaNotFoundException.class)
                .verify();
    }

    private void verify_Normalizer_HasAccept_Model_1_times() {
        verify(normalizer, times(1)).normalize(any(), anyCollection());
    }

    private void verify_DynamicModelDao_HasAccept_Model_1_times(JsonDynamicModel model) {
        verify(dao, times(1)).create(modelCaptor.capture(), anyString());

        JsonDynamicModel capturedModel = modelCaptor.getValue();

        assertEquals(model.getId(), capturedModel.getId());
        assertEquals(model.getModelName(), capturedModel.getModelName());

        capturedModel.getModelData().remove(created.name());
        capturedModel.getModelData().remove(modified.name());
        capturedModel.getModelData().remove(Model.FIELDS.model.name());
        capturedModel.getModelData().remove(version.name());
        assertEquals(model.getModelData(), capturedModel.getModelData());
    }

    private void verify_Validator_HasAccept_Model_1_times(JsonDynamicModel model) {
        verify(modelValidator, times(1)).validate(modelCaptor.capture());

        assertEquals(model, modelCaptor.getValue());
    }
}
