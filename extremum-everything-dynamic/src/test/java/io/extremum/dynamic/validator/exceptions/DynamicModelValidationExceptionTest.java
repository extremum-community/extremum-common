package io.extremum.dynamic.validator.exceptions;

import io.extremum.dynamic.validator.Violation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class DynamicModelValidationExceptionTest {
    @Test
    void toStringTest() {
        Set<Violation> violations = Stream.of("violation 1", "violation 2", "violation 3")
                .map(m -> (Violation) () -> m)
                .collect(Collectors.toSet());

        DynamicModelValidationException ex = new DynamicModelValidationException(violations);
        String toString = ex.toString();

        Assertions.assertTrue(toString.matches("^Violations: <violation \\d>; <violation \\d>; <violation \\d>$"));
    }

    @Test
    void getViolationsTest() {
        Set<Violation> violations = Stream.of("violation 1", "violation 2", "violation 3")
                .map(m -> (Violation) () -> m)
                .collect(Collectors.toSet());

        DynamicModelValidationException ex = new DynamicModelValidationException(violations);

        Set<Violation> violationsFromEx = ex.getViolations();

        Assertions.assertNotNull(violationsFromEx);
        Assertions.assertEquals(3, violationsFromEx.size());

        assertContainsViolation(violationsFromEx, "violation 1");
        assertContainsViolation(violationsFromEx, "violation 2");
        assertContainsViolation(violationsFromEx, "violation 3");
    }

    private void assertContainsViolation(Set<Violation> violationsFromEx, String expected) {
        boolean matched = violationsFromEx.stream()
                .anyMatch(v -> v.getMessage().equals(expected));

        Assertions.assertTrue(matched, "Violation " + expected + "doesn't contains in violations set");
    }
}