package io.extremum.dynamic.validator.services;

import io.extremum.dynamic.models.DynamicModel;
import io.extremum.dynamic.schema.Schema;
import io.extremum.dynamic.validator.Violation;
import io.extremum.dynamic.validator.exceptions.SchemaValidationException;

import java.util.Set;

public interface DynamicModelValidator<Model extends DynamicModel<?>, S extends Schema<?>> {
    void validate(Model model, S provider) throws SchemaValidationException;

    Set<Violation> validateSilence(Model model, S provider);
}
