package com.extremum.everything.security;

/**
 * @author rpuch
 */
public interface RoleChecker {
    boolean currentUserHasOneOf(String ... roles);
}
