package com.extremum.everything.security;

import com.extremum.sharedmodels.descriptor.Descriptor;

/**
 * @author rpuch
 */
public class AllowEverything implements EverythingRoleSecurity {
    @Override
    public void checkAllowedGet(Descriptor id) {
        // allow anything
    }

    @Override
    public void checkAllowedPatch(Descriptor id) {
        // allow anything
    }

    @Override
    public void checkAllowedRemove(Descriptor id) {
        // allow anything
    }
}
