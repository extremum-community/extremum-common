package com.extremum.security;

/**
 * @author rpuch
 */
public interface RoleChecker {
    boolean currentUserHasOneRoleOf(String ... roles);
}
