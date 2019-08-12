package io.extremum.security;

import io.extremum.sharedmodels.descriptor.Descriptor;

/**
 * @author rpuch
 */
public interface RoleSecurity {
    void checkGetAllowed(Descriptor id);

    void checkPatchAllowed(Descriptor id);

    void checkRemovalAllowed(Descriptor id);
}
