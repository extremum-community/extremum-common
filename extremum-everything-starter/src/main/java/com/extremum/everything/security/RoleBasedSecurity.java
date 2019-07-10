package com.extremum.everything.security;

/**
 * @author rpuch
 */
public interface RoleBasedSecurity {
    boolean currentUserHasOneOf(String ... roles);
}
