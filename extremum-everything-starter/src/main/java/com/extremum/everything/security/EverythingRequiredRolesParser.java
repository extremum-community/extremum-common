package com.extremum.everything.security;

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
