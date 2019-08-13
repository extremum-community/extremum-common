package io.extremum.authentication.api.interfaces;

import java.util.Optional;

public interface SecurityProvider {

    Optional<Object> getPrincipal();

    boolean hasAnyOfRoles(String... roles);

    <T> T getSessionExtension();

    <T> T getIdentityExtension();
}
