package com.extremum.everything.security;

import java.util.Optional;

/**
 * @author rpuch
 */
public interface PrincipalSource {
    Optional<String> getPrincipal();
}
