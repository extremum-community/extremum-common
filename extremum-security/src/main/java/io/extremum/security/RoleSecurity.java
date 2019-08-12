package io.extremum.security;

import io.extremum.sharedmodels.descriptor.Descriptor;

/**
 * @author rpuch
 */
public interface RoleSecurity {
    void checkGetAllowed(Descriptor id) throws ExtremumSecurityException;

    void checkPatchAllowed(Descriptor id) throws ExtremumSecurityException;

    void checkRemovalAllowed(Descriptor id) throws ExtremumSecurityException;

    void checkWatchAllowed(Descriptor id) throws ExtremumSecurityException;
}
