package com.extremum.everything.security;

/**
 * @author rpuch
 */
public class AllowAnyRoleChecker implements RoleChecker {
    @Override
    public boolean currentUserHasOneOf(String... roles) {
        return true;
    }
}
