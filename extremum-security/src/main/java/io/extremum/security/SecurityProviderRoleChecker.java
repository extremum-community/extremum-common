package io.extremum.security;

import io.extremum.authentication.api.SecurityProvider;

/**
 * @author rpuch
 */
public class SecurityProviderRoleChecker implements RoleChecker {
    private final SecurityProvider securityProvider;

    public SecurityProviderRoleChecker(SecurityProvider securityProvider) {
        this.securityProvider = securityProvider;
    }

    @Override
    public boolean currentUserHasOneRoleOf(String... roles) {
        return securityProvider.hasAnyOfRoles(roles);
    }
}
