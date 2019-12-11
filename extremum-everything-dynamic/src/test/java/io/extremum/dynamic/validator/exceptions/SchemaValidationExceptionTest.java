package io.extremum.dynamic.validator.exceptions;

import io.extremum.dynamic.validator.Violation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class SchemaValidationExceptionTest {
    @Test
    void toStringTest() {
        Set<Violation> violations = Stream.of("violation 1", "violation 2", "violation 3")
                .map(m -> (Violation) () -> m)
                .collect(Collectors.toSet());

        SchemaValidationException ex = new SchemaValidationException(violations);
        String toString = ex.toString();

        Assertions.assertTrue(toString.matches("^Violations: <violation \\d>; <violation \\d>; <violation \\d>$"));
    }
}