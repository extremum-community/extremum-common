package integration.io.extremum.dynamic.impl.JsonBasedMongoDynamicModelServiceTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import io.extremum.dynamic.dao.MongoBsonDynamicModelDao;
import io.extremum.dynamic.metadata.impl.DefaultJsonDynamicModelMetadataProvider;
import io.extremum.dynamic.models.impl.BsonDynamicModel;
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
import org.bson.Document;
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

import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import static io.atlassian.fugue.Try.successful;
import static io.extremum.dynamic.TestUtils.loadResourceAsInputStream;
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
    MongoBsonDynamicModelDao dao;

    @Mock
    DefaultJsonDynamicModelMetadataProvider metadataProvider;

    @Captor
    ArgumentCaptor<JsonDynamicModel> jModelCaptor;

    @Captor
    ArgumentCaptor<BsonDynamicModel> bModelCaptor;

    @Test
    void saveModel() throws IOException {
        final JsonDynamicModel jModel = mock(JsonDynamicModel.class);
        final BsonDynamicModel bModel = mock(BsonDynamicModel.class);

        NetworkntSchema schema = mock(NetworkntSchema.class);

        String schemaName = "test_schema";

        when(schemaProvider.loadSchema(schemaName)).thenReturn(schema);
        when(jModel.getModelData()).thenReturn(new HashMap<>());
        when(bModel.getModelData()).thenReturn(new Document());
        when(jModel.getModelName()).thenReturn(schemaName);
        when(bModel.getModelName()).thenReturn(schemaName);

        doReturn(just(successful(mock(ValidationContext.class)))).when(modelValidator).validate(any());

        when(dao.create(any(), anyString())).thenReturn(just(bModel));

        JsonBasedDynamicModelService service = new JsonBasedDynamicModelService(dao, modelValidator, metadataProvider,
                normalizer, datesProcessor, mapper);

        Mono<JsonDynamicModel> saved = service.saveModel(jModel);

        StepVerifier.setDefaultTimeout(Duration.of(30, ChronoUnit.SECONDS));

        StepVerifier.create(saved)
                .assertNext(resultModel -> {
                    assertEquals(resultModel.getId(), jModel.getId());
                    assertEquals(resultModel.getModelName(), jModel.getModelName());
                })
                .verifyComplete();
        JsonDynamicModelResponseDto.java
        verify_Validator_HasAccept_Model_1_times(jModel);
        verify_Normalizer_HasAccept_Model_1_times();
        verify_DynamicModelDao_HasAccept_Model_1_times(bModel);
    }

    @Test
    void notValidModelIsNotSaves() throws IOException {
        JsonBasedDynamicModelService service = new JsonBasedDynamicModelService(dao, modelValidator, metadataProvider,
                normalizer, datesProcessor, mapper);

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
                normalizer, datesProcessor, mapper);

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

    private void verify_DynamicModelDao_HasAccept_Model_1_times(BsonDynamicModel model) {
        verify(dao, times(1)).create(bModelCaptor.capture(), anyString());

        BsonDynamicModel capturedModel = bModelCaptor.getValue();
        assertEquals(model.getId(), capturedModel.getId());
        assertEquals(model.getModelName(), capturedModel.getModelName());

        capturedModel.getModelData().remove("created");
        capturedModel.getModelData().remove("modified");
        capturedModel.getModelData().remove("model");
        capturedModel.getModelData().remove("version");
        assertEquals(model.getModelData(), capturedModel.getModelData());
    }

    private void verify_Validator_HasAccept_Model_1_times(JsonDynamicModel model) {
        verify(modelValidator, times(1)).validate(jModelCaptor.capture());

        assertEquals(model, jModelCaptor.getValue());
    }
}
