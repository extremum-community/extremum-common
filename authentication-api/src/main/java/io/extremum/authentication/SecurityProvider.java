package io.extremum.authentication;

public interface SecurityProvider {

    Object getPrincipal();

    boolean hasAnyOfRoles(String... roles);

    <T> T getSessionExtension();
}
