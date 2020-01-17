package io.extremum.dynamic.validator.services.impl.networknt;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.atlassian.fugue.Try;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class NetworkntJsonDynamicModelValidatorTest {
    NetworkntSchemaProvider provider = new FileSystemNetworkntSchemaProvider(
            JsonSchemaType.V2019_09, makeBasicDirectory());

    private ObjectMapper mapper = new ObjectMapper();

    JsonDynamicModelValidator validator = new NetworkntJsonDynamicModelValidator(provider, mapper);

    @Test
    void validate_ok() throws IOException {
        Map<String, Object> modelData = stringToMap("{\"a\":\"b\", \"fieldDate1\": \"2013-01-09T09:31:26.111111-0500\"}");
        JsonDynamicModel model = new JsonDynamicModel("simple.schema.json", modelData);

        assertDoesNotThrow(() -> validator.validate(model));
    }

    @Test
    void validate_ok_validationContextContainsAPathsToDateFields() throws IOException {
        Map<String, Object> modelData = stringToMap("{\"a\":\"b\", " +
                "\"fieldDate1\": \"2013-01-09T09:31:26.111111-0500\", " +
                "\"fieldDate2\": \"2014-01-09T09:31:26.111111-0500\", " +
                "\"fieldDate3_noCybernatedDate\": \"2014-01-09T09:31:26.111111-0500\"}");

        JsonDynamicModel model = new JsonDynamicModel("simple.schema.json", modelData);

        Mono<Try<ValidationContext>> result = validator.validate(model);

        StepVerifier.setDefaultTimeout(Duration.of(30L, ChronoUnit.SECONDS));

        StepVerifier.create(result)
                .assertNext(tryInstance -> {
                    assertTrue(tryInstance.isSuccess(), "Try must contains a ValidationContext value");

                    tryInstance.fold(
                            e -> fail("Try must contains a ValidationContext instance nut contains an exception", e),
                            ctx -> {
                                assertEquals(2, ctx.getPaths().size());
                                return null;
                            }
                    );
                }).verifyComplete();
    }

    @Test
    void validate_violations_throws() throws IOException {
        Map<String, Object> modelData = stringToMap("{\"fieldDate1\":\"string\"}");
        JsonDynamicModel model = new JsonDynamicModel("simple.schema.json", modelData);

        Mono<Try<ValidationContext>> validationResult = validator.validate(model);

        StepVerifier.setDefaultTimeout(Duration.of(30, ChronoUnit.SECONDS));
        StepVerifier.create(validationResult)
                .assertNext(tryInstance -> {
                    assertTrue(tryInstance.isFailure());

                    tryInstance.recover(e -> {
                        assertTrue(e instanceof DynamicModelValidationException);
                        return null;
                    });
                })
                .verifyComplete();
    }

    @Test
    void validate_simple_schema_not_found() throws IOException {
        Map<String, Object> modelData = stringToMap("{\"field2\":\"string\"}");
        JsonDynamicModel model = new JsonDynamicModel("unknown_schema", modelData);

        Mono<Try<ValidationContext>> validationResult = validator.validate(model);

        StepVerifier.setDefaultTimeout(Duration.of(30, ChronoUnit.SECONDS));

        StepVerifier.create(validationResult)
                .assertNext(tryInstance -> {
                    assertTrue(tryInstance.isFailure());

                    tryInstance.recover(e -> {
                        assertTrue(e instanceof SchemaNotFoundException);
                        return null;
                    });
                })
                .verifyComplete();
    }

    @Test
    void validate_complex_schema_by_ref_not_found() throws IOException {
        Map<String, Object> modelData = stringToMap("{\"field2\":\"string\"}");
        JsonDynamicModel model = new JsonDynamicModel("complex_with_bad_ref.schema.json", modelData);

        Mono<Try<ValidationContext>> validationResult = validator.validate(model);

        StepVerifier.setDefaultTimeout(Duration.of(30, ChronoUnit.SECONDS));

        StepVerifier.create(validationResult)
                .assertNext(tryInstance -> {
                    assertTrue(tryInstance.isFailure());

                    tryInstance.recover(e -> {
                        assertTrue(e instanceof SchemaNotFoundException);
                        return null;
                    });
                })
                .verifyComplete();
    }

    private Path makeBasicDirectory() {
        String pathToFile = this.getClass().getClassLoader().getResource("test.file.txt").getPath();
        String base = pathToFile.substring(0, pathToFile.lastIndexOf("/"));
        return Paths.get(base, "schemas");
    }

    private Map<String, Object> stringToMap(@Language("JSON") String value) throws IOException {
        JsonNode node = mapper.readValue(value, JsonNode.class);
        return mapper.convertValue(node, new TypeReference<Map<String, Object>>() {
        });
    }
}