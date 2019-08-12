package io.extremum.security;

import java.util.Optional;

/**
 * @author rpuch
 */
public interface CheckerContext {
    Optional<String> getCurrentPrincipal();

    boolean currentUserHasOneOf(String... roles);
}
