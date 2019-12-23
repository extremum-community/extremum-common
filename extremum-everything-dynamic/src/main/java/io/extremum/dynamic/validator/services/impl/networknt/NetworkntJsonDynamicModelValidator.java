package io.extremum.dynamic.validator.services.impl.networknt;

import com.networknt.schema.ValidationMessage;
import io.atlassian.fugue.Either;
import io.atlassian.fugue.Unit;
import io.extremum.dynamic.models.impl.JsonDynamicModel;
import io.extremum.dynamic.schema.networknt.NetworkntSchema;
import io.extremum.dynamic.schema.provider.networknt.NetworkntSchemaProvider;
import io.extremum.dynamic.validator.Violation;
import io.extremum.dynamic.validator.exceptions.DynamicModelValidationException;
import io.extremum.dynamic.validator.exceptions.SchemaLoadingException;
import io.extremum.dynamic.validator.services.impl.JsonDynamicModelValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class NetworkntJsonDynamicModelValidator implements JsonDynamicModelValidator {
    private final NetworkntSchemaProvider schemaProvider;

    @Override
    public Mono<Either<Exception, Unit>> validate(JsonDynamicModel model) {
        try {
            NetworkntSchema schema = schemaProvider.loadSchema(model.getModelName());

            Set<ValidationMessage> validationMessages = schema.getSchema().validate(model.getModelData());
            if (!validationMessages.isEmpty()) {
                DynamicModelValidationException ex = new DynamicModelValidationException(toViolationSet(validationMessages));
                log.warn("Model {} is invalid", model, ex);
                return Mono.just(Either.left(ex));
            } else {
                return Mono.just(Either.right(Unit.Unit()));
            }
        } catch (SchemaLoadingException e) {
            log.error("Unable to validate a model {}: schema not found", model, e);
            return Mono.just(Either.left(e));
        }
    }

    private Set<Violation> toViolationSet(Set<ValidationMessage> messages) {
        return messages.stream()
                .map(msg -> (Violation) msg::getMessage)
                .collect(Collectors.toSet());
    }
}
