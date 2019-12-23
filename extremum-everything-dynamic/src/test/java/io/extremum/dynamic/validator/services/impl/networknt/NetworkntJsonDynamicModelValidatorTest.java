package io.extremum.dynamic.validator.services.impl.networknt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.atlassian.fugue.Either;
import io.atlassian.fugue.Unit;
import io.extremum.dynamic.models.impl.JsonDynamicModel;
import io.extremum.dynamic.schema.JsonSchemaType;
import io.extremum.dynamic.schema.provider.networknt.NetworkntSchemaProvider;
import io.extremum.dynamic.schema.provider.networknt.impl.FileSystemNetworkntSchemaProvider;
import io.extremum.dynamic.validator.exceptions.DynamicModelValidationException;
import io.extremum.dynamic.validator.exceptions.SchemaNotFoundException;
import io.extremum.dynamic.validator.services.impl.JsonDynamicModelValidator;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class NetworkntJsonDynamicModelValidatorTest {
    NetworkntSchemaProvider provider = new FileSystemNetworkntSchemaProvider(
            JsonSchemaType.V2019_09, makeBasicDirectory());

    JsonDynamicModelValidator validator = new NetworkntJsonDynamicModelValidator(provider);

    @Test
    void validate_ok() throws IOException {
        JsonNode modelData = stringToJsonNode("{\"a\":\"b\"}");
        JsonDynamicModel model = new JsonDynamicModel("schemas/simple.schema.json", modelData);

        assertDoesNotThrow(() -> validator.validate(model));
    }

    @Test
    void validate_violations_throws() throws IOException {
        JsonNode modelData = stringToJsonNode("{\"field2\":\"string\"}");
        JsonDynamicModel model = new JsonDynamicModel("schemas/simple.schema.json", modelData);

        Mono<Either<Exception, Unit>> validationResult = validator.validate(model);

        StepVerifier.setDefaultTimeout(Duration.of(30, ChronoUnit.SECONDS));
        StepVerifier.create(validationResult)
                .expectNextMatches(either -> either.isLeft() &&
                        either.left().get() instanceof DynamicModelValidationException)
                .verifyComplete();
    }

    @Test
    void validate_simple_schema_not_found() throws IOException {
        JsonNode modelData = stringToJsonNode("{\"field2\":\"string\"}");
        JsonDynamicModel model = new JsonDynamicModel("unknown_schema", modelData);

        Mono<Either<Exception, Unit>> validationResult = validator.validate(model);

        StepVerifier.setDefaultTimeout(Duration.of(30, ChronoUnit.SECONDS));

        StepVerifier.create(validationResult)
                .expectNextMatches(either -> either.isLeft() &&
                        either.left().get() instanceof SchemaNotFoundException)
                .verifyComplete();
    }

    @Test
    void validate_complex_schema_by_ref_not_found() throws IOException {
        JsonNode modelData = stringToJsonNode("{\"field2\":\"string\"}");
        JsonDynamicModel model = new JsonDynamicModel("schemas/complex_with_bad_ref.schema.json", modelData);

        Mono<Either<Exception, Unit>> validationResult = validator.validate(model);

        StepVerifier.setDefaultTimeout(Duration.of(30, ChronoUnit.SECONDS));

        StepVerifier.create(validationResult)
                .expectNextMatches(either -> either.isLeft() &&
                        either.left().get() instanceof SchemaNotFoundException)
                .verifyComplete();
    }

    private Path makeBasicDirectory() {
        String pathToFile = this.getClass().getClassLoader().getResource("test.file.txt").getPath();
        String base = pathToFile.substring(0, pathToFile.lastIndexOf("/"));
        return Paths.get(base);
    }

    private JsonNode stringToJsonNode(String value) throws IOException {
        return new ObjectMapper().readValue(value, JsonNode.class);
    }
}