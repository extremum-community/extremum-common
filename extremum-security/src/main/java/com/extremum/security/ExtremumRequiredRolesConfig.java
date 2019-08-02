package com.extremum.security;

/**
 * @author rpuch
 */
class ExtremumRequiredRolesConfig {
    private final String[] defaultRoles;
    private final String[] getRoles;
    private final String[] patchRoles;
    private final String[] removalRoles;

    ExtremumRequiredRolesConfig(String[] defaultRoles, String[] getRoles, String[] patchRoles,
            String[] removalRoles) {
        this.defaultRoles = defaultRoles;
        this.getRoles = getRoles;
        this.patchRoles = patchRoles;
        this.removalRoles = removalRoles;
    }

    String[] rolesForGet() {
        return firstNonEmpty(getRoles, defaultRoles);
    }

    String[] rolesForPatch() {
        return firstNonEmpty(patchRoles, defaultRoles);
    }

    String[] rolesForRemove() {
        return firstNonEmpty(removalRoles, defaultRoles);
    }

    private String[] firstNonEmpty(String[] first, String[] second) {
        if (first.length > 0) {
            return first;
        }
        return second;
    }
}
