package com.extremum.everything.security;

import com.extremum.sharedmodels.descriptor.Descriptor;

/**
 * @author rpuch
 */
public class AllowEverything implements EverythingSecurity {
    @Override
    public void checkRolesAllowCurrentUserToGet(Descriptor id) {
        // allow anything
    }

    @Override
    public void checkRolesAllowCurrentUserToPatch(Descriptor id) {
        // allow anything
    }

    @Override
    public void checkRolesAllowCurrentUserToRemove(Descriptor id) {
        // allow anything
    }
}
