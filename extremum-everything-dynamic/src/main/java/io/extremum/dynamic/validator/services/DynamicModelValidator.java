package io.extremum.dynamic.validator.services;

import io.extremum.dynamic.models.DynamicModel;
import io.extremum.dynamic.validator.exceptions.SchemaValidationException;

public interface DynamicModelValidator<Model extends DynamicModel<?>> {
    void validate(Model model) throws SchemaValidationException;
}
