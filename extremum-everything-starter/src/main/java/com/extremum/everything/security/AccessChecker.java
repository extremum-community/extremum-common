package com.extremum.everything.security;

/**
 * @author rpuch
 */
public interface AccessChecker {
    boolean currentUserHasOneOf(String ... roles);
}
