package io.extremum.security;

import reactor.core.publisher.Mono;

/**
 * @author rpuch
 */
public interface ReactivePrincipalSource {
    Mono<String> getPrincipal();
}
