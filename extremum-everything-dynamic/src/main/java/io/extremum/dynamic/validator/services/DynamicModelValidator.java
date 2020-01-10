package io.extremum.dynamic.validator.services;

import io.atlassian.fugue.Either;
import io.extremum.dynamic.models.DynamicModel;
import io.extremum.dynamic.validator.ValidationContext;
import io.extremum.dynamic.validator.exceptions.DynamicModelValidationException;
import io.extremum.dynamic.validator.exceptions.SchemaNotFoundException;
import reactor.core.publisher.Mono;

public interface DynamicModelValidator<Model extends DynamicModel<?>> {
    /**
     * Mono can contains {@link DynamicModelValidationException}, {@link SchemaNotFoundException}
     */
    Mono<Either<Exception, ValidationContext>> validate(Model model);
}
