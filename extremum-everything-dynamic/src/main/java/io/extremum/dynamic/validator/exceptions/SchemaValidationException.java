package io.extremum.dynamic.validator.exceptions;

import io.extremum.dynamic.validator.Violation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@Getter
@RequiredArgsConstructor
public class SchemaValidationException extends Exception {
    private final Set<Violation> violations;
}
