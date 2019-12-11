package integration.io.extremum.dynamic.impl.JsonBasedMongoDynamicModelServiceTest;

import integration.io.extremum.dynamic.EmptyConfiguration;
import io.extremum.dynamic.MongoSchemaPointer;
import io.extremum.dynamic.dao.impl.MongoDynamicModelDao;
import io.extremum.dynamic.models.impl.JsonBasedDynamicModel;
import io.extremum.dynamic.schema.networknt.MongoBasedSchemaProvider;
import io.extremum.dynamic.schema.networknt.NetworkntSchema;
import io.extremum.dynamic.services.impl.JsonBasedMongoDynamicModelService;
import io.extremum.dynamic.validator.services.impl.JsonBasedDynamicModelValidator;
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
class JsonBasedMongoDynamicModelServiceTest {
    @MockBean
    MongoBasedSchemaProvider schemaProvider;

    @MockBean
    JsonBasedDynamicModelValidator modelValidator;

    @MockBean
    MongoDynamicModelDao dao;

    @Captor
    ArgumentCaptor<MongoSchemaPointer> pointerCaptor;

    @Captor
    ArgumentCaptor<JsonBasedDynamicModel> modelCaptor;

    @Captor
    ArgumentCaptor<NetworkntSchema> schemaCaptor;

    @Test
    void saveModel() {
        MongoSchemaPointer pointer = Mockito.mock(MongoSchemaPointer.class);
        final JsonBasedDynamicModel model = Mockito.mock(JsonBasedDynamicModel.class);

        NetworkntSchema schema = Mockito.mock(NetworkntSchema.class);

        configureBehavior(pointer, model, schema);

        JsonBasedMongoDynamicModelService service = new JsonBasedMongoDynamicModelService(dao, schemaProvider, modelValidator);

        Mono<JsonBasedDynamicModel> saved = service.saveModel(pointer, model);

        verify_SchemaProvider_HasAccept_SchemaPointer_1_times(pointer);
        verify_Validator_HasAccept_Model_Schema_1_times(model, schema);
        verify_DynamicModelDao_HasAccept_Pointer_Model_1_times(pointer, model);

        StepVerifier.setDefaultTimeout(Duration.of(30, ChronoUnit.SECONDS));

        StepVerifier.create(saved)
                .assertNext(resultModel -> Assertions.assertEquals(resultModel, model))
                .verifyComplete();
    }

    private void verify_DynamicModelDao_HasAccept_Pointer_Model_1_times(MongoSchemaPointer pointer, JsonBasedDynamicModel model) {
        Mockito.verify(dao, Mockito.times(1)).save(modelCaptor.capture());

        Assertions.assertEquals(model, modelCaptor.getValue());
    }

    private void verify_SchemaProvider_HasAccept_SchemaPointer_1_times(MongoSchemaPointer pointer) {
        Mockito.verify(schemaProvider, Mockito.times(1)).loadSchema(pointerCaptor.capture());
        MongoSchemaPointer value = pointerCaptor.getValue();
        Assertions.assertEquals(value.getPointer(), pointer.getPointer());
    }

    private void verify_Validator_HasAccept_Model_Schema_1_times(JsonBasedDynamicModel model, NetworkntSchema schema) {
        Mockito.verify(modelValidator, Mockito.times(1)).validate(modelCaptor.capture(), schemaCaptor.capture());

        Assertions.assertEquals(model, modelCaptor.getValue());
        Assertions.assertEquals(schema, schemaCaptor.getValue());
    }

    private void configureBehavior(MongoSchemaPointer pointer, JsonBasedDynamicModel model, NetworkntSchema schema) {
        Mockito.when(schemaProvider.loadSchema(pointer)).thenReturn(schema);

        Mockito.doNothing().when(modelValidator).validate(model, schema);

        Mockito.when(dao.save(model)).thenReturn(Mono.just(model));
    }
}
