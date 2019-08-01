package com.extremum.everything.security;

/**
 * @author rpuch
 */
public interface CheckerContext {
    String getCurrentPrincipal();

    boolean currentUserHasOneOf(String... roles);
}
