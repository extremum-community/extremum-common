package io.extremum.security;

import io.extremum.authentication.api.ReactiveSecurityProvider;
import reactor.core.publisher.Mono;

/**
 * @author rpuch
 */
public class SecurityProviderReactivePrincipalSource implements ReactivePrincipalSource {
    private final ReactiveSecurityProvider securityProvider;

    public SecurityProviderReactivePrincipalSource(ReactiveSecurityProvider securityProvider) {
        this.securityProvider = securityProvider;
    }

    @Override
    public Mono<String> getPrincipal() {
        return securityProvider.getPrincipal().map(Object::toString);
    }
}
