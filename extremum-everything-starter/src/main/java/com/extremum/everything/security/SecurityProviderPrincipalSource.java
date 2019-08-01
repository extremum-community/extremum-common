package com.extremum.everything.security;

import io.extremum.authentication.api.SecurityProvider;

/**
 * @author rpuch
 */
public class SecurityProviderPrincipalSource implements PrincipalSource {
    private final SecurityProvider securityProvider;

    public SecurityProviderPrincipalSource(SecurityProvider securityProvider) {
        this.securityProvider = securityProvider;
    }

    @Override
    public String getPrincipal() {
        Object principal = securityProvider.getPrincipal();
        return principal == null ? null : principal.toString();
    }
}
