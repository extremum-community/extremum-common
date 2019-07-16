package com.extremum.everything.security;

/**
 * @author rpuch
 */
class EverythingSecuredConfig {
    private final Access defaultAccess;
    private final Access getAccess;
    private final Access patchAccess;
    private final Access removeAccess;

    EverythingSecuredConfig(Access defaultAccess, Access getAccess, Access patchAccess,
            Access removeAccess) {
        this.defaultAccess = defaultAccess;
        this.getAccess = getAccess;
        this.patchAccess = patchAccess;
        this.removeAccess = removeAccess;
    }

    String[] rolesForGet() {
        return firstNonEmpty(getAccess, defaultAccess);
    }

    String[] rolesForPatch() {
        return firstNonEmpty(patchAccess, defaultAccess);
    }

    String[] rolesForRemove() {
        return firstNonEmpty(removeAccess, defaultAccess);
    }

    private String[] firstNonEmpty(Access first, Access second) {
        if (first.value().length > 0) {
            return first.value();
        }
        return second.value();
    }
}
