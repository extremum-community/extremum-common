package io.extremum.dynamic.validator.services.impl.networknt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.atlassian.fugue.Either;
import io.extremum.dynamic.models.impl.JsonDynamicModel;
import io.extremum.dynamic.schema.JsonSchemaType;
import io.extremum.dynamic.schema.provider.networknt.NetworkntSchemaProvider;
import io.extremum.dynamic.schema.provider.networknt.impl.FileSystemNetworkntSchemaProvider;
import io.extremum.dynamic.validator.ValidationContext;
import io.extremum.dynamic.validator.exceptions.DynamicModelValidationException;
import io.extremum.dynamic.validator.exceptions.SchemaNotFoundException;
import io.extremum.dynamic.validator.services.impl.JsonDynamicModelValidator;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class NetworkntJsonDynamicModelValidatorTest {
    NetworkntSchemaProvider provider = new FileSystemNetworkntSchemaProvider(
            JsonSchemaType.V2019_09, makeBasicDirectory());

    JsonDynamicModelValidator validator = new NetworkntJsonDynamicModelValidator(provider);

    @Test
    void validate_ok() throws IOException {
        JsonNode modelData = stringToJsonNode("{\"a\":\"b\", \"fieldDate1\": \"2013-01-09T09:31:26.111111-0500\"}");
        JsonDynamicModel model = new JsonDynamicModel("simple.schema.json", modelData);

        assertDoesNotThrow(() -> validator.validate(model));
    }

    @Test
    void validate_ok_validationContextContainsAPathsToDateFields() throws IOException {
        JsonNode modelData = stringToJsonNode("{\"a\":\"b\", " +
                "\"fieldDate1\": \"2013-01-09T09:31:26.111111-0500\", " +
                "\"fieldDate2\": \"2014-01-09T09:31:26.111111-0500\", " +
                "\"fieldDate3_noCybernatedDate\": \"2014-01-09T09:31:26.111111-0500\"}");

        JsonDynamicModel model = new JsonDynamicModel("simple.schema.json", modelData);

        Mono<Either<Exception, ValidationContext>> result = validator.validate(model);

        StepVerifier.setDefaultTimeout(Duration.of(30L, ChronoUnit.SECONDS));

        StepVerifier.create(result)
                .assertNext(either -> {
                    assertTrue(either.isRight(), () -> either.left().get().toString());
                    assertEquals(2, either.right().get().getPaths().size());
                }).verifyComplete();
    }

    @Test
    void validate_violations_throws() throws IOException {
        JsonNode modelData = stringToJsonNode("{\"fieldDate1\":\"string\"}");
        JsonDynamicModel model = new JsonDynamicModel("simple.schema.json", modelData);

        Mono<Either<Exception, ValidationContext>> validationResult = validator.validate(model);

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

        Mono<Either<Exception, ValidationContext>> validationResult = validator.validate(model);

        StepVerifier.setDefaultTimeout(Duration.of(30, ChronoUnit.SECONDS));

        StepVerifier.create(validationResult)
                .expectNextMatches(either -> either.isLeft() &&
                        either.left().get() instanceof SchemaNotFoundException)
                .verifyComplete();
    }

    @Test
    void validate_complex_schema_by_ref_not_found() throws IOException {
        JsonNode modelData = stringToJsonNode("{\"field2\":\"string\"}");
        JsonDynamicModel model = new JsonDynamicModel("complex_with_bad_ref.schema.json", modelData);

        Mono<Either<Exception, ValidationContext>> validationResult = validator.validate(model);

        StepVerifier.setDefaultTimeout(Duration.of(30, ChronoUnit.SECONDS));

        StepVerifier.create(validationResult)
                .expectNextMatches(either -> either.isLeft() &&
                        either.left().get() instanceof SchemaNotFoundException)
                .verifyComplete();
    }

    private Path makeBasicDirectory() {
        String pathToFile = this.getClass().getClassLoader().getResource("test.file.txt").getPath();
        String base = pathToFile.substring(0, pathToFile.lastIndexOf("/"));
        return Paths.get(base, "schemas");
    }

    private JsonNode stringToJsonNode(@Language("JSON") String value) throws IOException {
        return new ObjectMapper().readValue(value, JsonNode.class);
    }
}