package com.extremum.everything.security;

/**
 * @author rpuch
 */
public class AllowAnyRoleChecker implements RoleChecker {
    @Override
    public boolean currentUserHasOneRoleOf(String... roles) {
        return true;
    }
}
