package io.extremum.authentication.api;

public interface SecurityProvider {

    Object getPrincipal();

    boolean hasAnyOfRoles(String... roles);

    <T> T getSessionExtension();
}
