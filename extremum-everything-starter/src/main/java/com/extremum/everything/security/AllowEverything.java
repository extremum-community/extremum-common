package com.extremum.everything.security;

import com.extremum.sharedmodels.descriptor.Descriptor;

/**
 * @author rpuch
 */
public class AllowEverything implements EverythingRoleSecurity {
    @Override
    public void checkGetAllowed(Descriptor id) {
        // allow anything
    }

    @Override
    public void checkPatchAllowed(Descriptor id) {
        // allow anything
    }

    @Override
    public void checkRemovalAllowed(Descriptor id) {
        // allow anything
    }
}
