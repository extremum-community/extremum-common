package io.extremum.security;

import io.extremum.authentication.api.SecurityProvider;

import java.util.Optional;

/**
 * @author rpuch
 */
public class SecurityProviderPrincipalSource implements PrincipalSource {
    private final SecurityProvider securityProvider;

    public SecurityProviderPrincipalSource(SecurityProvider securityProvider) {
        this.securityProvider = securityProvider;
    }

    @Override
    public Optional<String> getPrincipal() {
        return securityProvider.getPrincipal().map(Object::toString);
    }
}
