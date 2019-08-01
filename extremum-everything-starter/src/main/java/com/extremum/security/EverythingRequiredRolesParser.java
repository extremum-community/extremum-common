package com.extremum.security;

/**
 * @author rpuch
 */
public class EverythingRequiredRolesParser {
    public EverythingRequiredRolesConfig parse(EverythingRequiredRoles everythingRequiredRoles) {
        return new EverythingRequiredRolesConfig(
                everythingRequiredRoles.defaultAccess(),
                everythingRequiredRoles.get(),
                everythingRequiredRoles.patch(),
                everythingRequiredRoles.remove()
        );
    }
}
