package com.extremum.everything.services.management;

import com.extremum.sharedmodels.descriptor.Descriptor;

/**
 * @author rpuch
 */
public class NullSecurity implements EverythingSecurity {
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
