package io.extremum.authentication.api;

import reactor.core.publisher.Mono;

public interface ReactiveSecurityProvider {

    Mono<Object> getPrincipal();

    Mono<Boolean> hasAnyOfRoles(String... roles);

    <T> Mono<T> getSessionExtension();

    <T> Mono<T> getIdentityExtension();
}
