package com.extremum.security;

import com.extremum.sharedmodels.descriptor.Descriptor;

/**
 * @author rpuch
 */
public interface EverythingRoleSecurity {
    void checkGetAllowed(Descriptor id);

    void checkPatchAllowed(Descriptor id);

    void checkRemovalAllowed(Descriptor id);
}
