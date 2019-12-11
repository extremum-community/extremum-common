package integration.io.extremum.dynamic.impl.JsonBasedMongoDynamicModelServiceTest;

import integration.io.extremum.dynamic.EmptyConfiguration;
import io.extremum.dynamic.MongoSchemaPointer;
import io.extremum.dynamic.dao.impl.MongoDynamicModelDao;
import io.extremum.dynamic.models.impl.JsonBasedDynamicModel;
import io.extremum.dynamic.schema.networknt.MongoBasedSchemaProvider;
import io.extremum.dynamic.schema.networknt.NetworkntSchema;
import io.extremum.dynamic.services.impl.JsonBasedDynamicModelService;
import io.extremum.dynamic.validator.exceptions.SchemaValidationException;
import io.extremum.dynamic.validator.services.impl.networknt.NetworkntSchemaProviderBasedDynamicModelValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@SpringBootTest(classes = EmptyConfiguration.class)
class JsonBasedDynamicModelServiceTest {
    @MockBean
    MongoBasedSchemaProvider schemaProvider;

    @MockBean
    NetworkntSchemaProviderBasedDynamicModelValidator modelValidator;

    @MockBean
    MongoDynamicModelDao dao;

    @Captor
    ArgumentCaptor<JsonBasedDynamicModel> modelCaptor;

    @Test
    void saveModel() {
        MongoSchemaPointer pointer = Mockito.mock(MongoSchemaPointer.class);
        final JsonBasedDynamicModel model = Mockito.mock(JsonBasedDynamicModel.class);

        NetworkntSchema schema = Mockito.mock(NetworkntSchema.class);

        configureBehavior(pointer, model, schema);

        JsonBasedDynamicModelService service = new JsonBasedDynamicModelService(dao, modelValidator);

        Mono<JsonBasedDynamicModel> saved = service.saveModel(model);

        verify_Validator_HasAccept_Model_1_times(model);
        verify_DynamicModelDao_HasAccept_Model_1_times(model);

        StepVerifier.setDefaultTimeout(Duration.of(30, ChronoUnit.SECONDS));

        StepVerifier.create(saved)
                .assertNext(resultModel -> Assertions.assertEquals(resultModel, model))
                .verifyComplete();
    }

    private void verify_DynamicModelDao_HasAccept_Model_1_times(JsonBasedDynamicModel model) {
        Mockito.verify(dao, Mockito.times(1)).save(modelCaptor.capture());

        Assertions.assertEquals(model, modelCaptor.getValue());
    }

    private void verify_Validator_HasAccept_Model_1_times(JsonBasedDynamicModel model) {
        try {
            Mockito.verify(modelValidator, Mockito.times(1)).validate(modelCaptor.capture());

            Assertions.assertEquals(model, modelCaptor.getValue());
        } catch (SchemaValidationException e) {
            Assertions.fail("Unexpected exception " + e);
        }
    }

    private void configureBehavior(MongoSchemaPointer pointer, JsonBasedDynamicModel model, NetworkntSchema schema) {
        Mockito.when(schemaProvider.loadSchema(pointer)).thenReturn(schema);

        try {
            Mockito.doNothing().when(modelValidator).validate(model);

            Mockito.when(dao.save(model)).thenReturn(Mono.just(model));
        } catch (SchemaValidationException e) {
            Assertions.fail("Unexpected exception " + e);
        }
    }
}
