package io.extremum.security;

import reactor.core.publisher.Mono;

/**
 * @author rpuch
 */
public interface ReactiveCheckerContext {
    Mono<String> getCurrentPrincipal();

    Mono<Boolean> currentUserHasOneOf(String... roles);
}
