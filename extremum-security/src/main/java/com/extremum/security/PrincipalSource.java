package com.extremum.security;

import java.util.Optional;

/**
 * @author rpuch
 */
public interface PrincipalSource {
    Optional<String> getPrincipal();
}
