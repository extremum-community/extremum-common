package io.extremum.dynamic.validator.services.impl.networknt;

import io.extremum.dynamic.models.impl.JsonBasedDynamicModel;
import io.extremum.dynamic.schema.networknt.MemorySchemaProvider;
import io.extremum.dynamic.validator.Violation;
import io.extremum.dynamic.validator.exceptions.SchemaValidationException;
import io.extremum.dynamic.validator.services.impl.SchemaProviderBasedDynamicModelValidator;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@RequiredArgsConstructor
public class NetworkntSchemaProviderBasedDynamicModelValidator implements SchemaProviderBasedDynamicModelValidator<MemorySchemaProvider> {
    private final MemorySchemaProvider schemaProvider;

    @Override
    public MemorySchemaProvider getSchemaProvider() {
        return schemaProvider;
    }

    @Override
    public void validate(JsonBasedDynamicModel model) throws SchemaValidationException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Set<Violation> validateSilence(JsonBasedDynamicModel model) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
