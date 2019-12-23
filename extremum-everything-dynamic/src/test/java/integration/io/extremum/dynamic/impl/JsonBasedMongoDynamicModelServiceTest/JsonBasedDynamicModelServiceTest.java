package integration.io.extremum.dynamic.impl.JsonBasedMongoDynamicModelServiceTest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import integration.io.extremum.dynamic.EmptyConfiguration;
import io.extremum.dynamic.dao.MongoJsonDynamicModelDao;
import io.extremum.dynamic.metadata.impl.DefaultJsonDynamicModelMetadataProvider;
import io.extremum.dynamic.models.impl.JsonDynamicModel;
import io.extremum.dynamic.schema.networknt.NetworkntSchema;
import io.extremum.dynamic.schema.provider.networknt.impl.FileSystemNetworkntSchemaProvider;
import io.extremum.dynamic.services.impl.JsonBasedDynamicModelService;
import io.extremum.dynamic.validator.exceptions.DynamicModelValidationException;
import io.extremum.dynamic.validator.exceptions.SchemaNotFoundException;
import io.extremum.dynamic.validator.services.impl.networknt.NetworkntJsonDynamicModelValidator;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static io.extremum.dynamic.TestUtils.loadResourceAsInputStream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = EmptyConfiguration.class)
class JsonBasedDynamicModelServiceTest {
    @MockBean
    FileSystemNetworkntSchemaProvider schemaProvider;

    @SpyBean
    NetworkntJsonDynamicModelValidator modelValidator;

    @Mock
    MongoJsonDynamicModelDao dao;

    @Mock
    DefaultJsonDynamicModelMetadataProvider metadataProvider;

    @Captor
    ArgumentCaptor<JsonDynamicModel> modelCaptor;

    @Test
    void saveModel() {
        final JsonDynamicModel model = Mockito.mock(JsonDynamicModel.class);

        NetworkntSchema schema = Mockito.mock(NetworkntSchema.class);

        String schemaName = "test_schema";

        configureBehavior(schemaName, model, schema);

        JsonBasedDynamicModelService service = new JsonBasedDynamicModelService(dao, modelValidator, metadataProvider);

        Mono<JsonDynamicModel> saved = service.saveModel(model);

        StepVerifier.setDefaultTimeout(Duration.of(30, ChronoUnit.SECONDS));

        StepVerifier.create(saved)
                .assertNext(resultModel -> assertEquals(resultModel, model))
                .verifyComplete();

        verify_Validator_HasAccept_Model_1_times(model);
        verify_DynamicModelDao_HasAccept_Model_1_times(model);
    }

    @Test
    void notValidModelIsNotSaves() throws IOException {
        JsonBasedDynamicModelService service = new JsonBasedDynamicModelService(dao, modelValidator, metadataProvider);

        String invalidModelRawValue = "{\"field1\":1}";

        JsonDynamicModel model = new JsonDynamicModel("model",
                new ObjectMapper().readValue(invalidModelRawValue, JsonNode.class));

        JsonSchema schema = JsonSchemaFactory.getInstance()
                .getSchema(loadResourceAsInputStream(this.getClass().getClassLoader(), "schemas/simple.schema.json"));

        when(schemaProvider.loadSchema(Mockito.any())).thenReturn(new NetworkntSchema(schema));

        Mono<JsonDynamicModel> result = service.saveModel(model);

        verify(dao, never()).save(eq(model), anyString());

        StepVerifier.setDefaultTimeout(Duration.of(30, ChronoUnit.SECONDS));

        StepVerifier.create(result)
                .expectError(DynamicModelValidationException.class)
                .verify();
    }

    @Test
    void modelIsNotSaved_schemaDoesntExists() {
        JsonBasedDynamicModelService service = new JsonBasedDynamicModelService(dao, modelValidator, metadataProvider);

        String unknownModel = "unknownModel";

        JsonDynamicModel model = new JsonDynamicModel(unknownModel, null);

        when(schemaProvider.loadSchema(unknownModel)).thenThrow(SchemaNotFoundException.class);

        Mono<JsonDynamicModel> result = service.saveModel(model);

        verify(dao, never()).save(eq(model), anyString());

        StepVerifier.setDefaultTimeout(Duration.of(30, ChronoUnit.SECONDS));

        StepVerifier.create(result)
                .expectError(SchemaNotFoundException.class)
                .verify();
    }

    private void verify_DynamicModelDao_HasAccept_Model_1_times(JsonDynamicModel model) {
        verify(dao, Mockito.times(1)).save(modelCaptor.capture(), anyString());

        assertEquals(model, modelCaptor.getValue());
    }

    private void verify_Validator_HasAccept_Model_1_times(JsonDynamicModel model) {
        try {
            verify(modelValidator, Mockito.times(1)).validate(modelCaptor.capture());

            assertEquals(model, modelCaptor.getValue());
        } catch (DynamicModelValidationException | SchemaNotFoundException e) {
            fail("Unexpected exception " + e);
        }
    }

    private void configureBehavior(String schemaName, JsonDynamicModel model, NetworkntSchema schema) {
        when(schemaProvider.loadSchema(schemaName)).thenReturn(schema);
        when(model.getModelName()).thenReturn(schemaName);

        try {
            Mockito.doNothing().when(modelValidator).validate(model);

            when(dao.save(eq(model), anyString())).thenReturn(Mono.just(model));
        } catch (DynamicModelValidationException | SchemaNotFoundException e) {
            fail("Unexpected exception " + e);
        }
    }
}
