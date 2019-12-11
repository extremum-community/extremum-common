package io.extremum.dynamic.validator.services.impl.networknt;

import com.networknt.schema.ValidationMessage;
import io.extremum.dynamic.models.impl.JsonDynamicModel;
import io.extremum.dynamic.schema.networknt.NetworkntSchema;
import io.extremum.dynamic.schema.networknt.NetworkntSchemaProvider;
import io.extremum.dynamic.validator.Violation;
import io.extremum.dynamic.validator.exceptions.DynamicModelValidationException;
import io.extremum.dynamic.validator.exceptions.SchemaNotFoundException;
import io.extremum.dynamic.validator.services.impl.JsonDynamicModelValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class NetworkntJsonDynamicModelValidator implements JsonDynamicModelValidator {
    private final NetworkntSchemaProvider schemaProvider;

    @Override
    public void validate(JsonDynamicModel model) throws DynamicModelValidationException, SchemaNotFoundException {
        NetworkntSchema schema = schemaProvider.loadSchema(model.getModelName());

        Set<ValidationMessage> validationMessages = schema.getSchema().validate(model.getModelData());
        if (!validationMessages.isEmpty()) {
            throw new DynamicModelValidationException(toViolationSet(validationMessages));
        }
    }

    private Set<Violation> toViolationSet(Set<ValidationMessage> messages) {
        return messages.stream()
                .map(msg -> (Violation) msg::getMessage)
                .collect(Collectors.toSet());
    }
}
