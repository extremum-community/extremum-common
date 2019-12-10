package io.extremum.dynamic.validator.services.impl;

import io.extremum.dynamic.models.impl.JsonBasedDynamicModel;
import io.extremum.dynamic.schema.networknt.NetworkntSchema;
import io.extremum.dynamic.validator.Violation;
import io.extremum.dynamic.validator.services.DynamicModelValidator;

import java.util.Set;

public class JsonBasedDynamicModelValidator implements DynamicModelValidator<JsonBasedDynamicModel, NetworkntSchema> {
    @Override
    public void validate(JsonBasedDynamicModel model, NetworkntSchema provider) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Set<Violation> validateSilence(JsonBasedDynamicModel model, NetworkntSchema provider) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
