package com.extremum.everything.security;

import com.extremum.sharedmodels.descriptor.Descriptor;

/**
 * @author rpuch
 */
public interface EverythingSecurity {
    void checkRolesAllowCurrentUserToGet(Descriptor id);

    void checkRolesAllowCurrentUserToPatch(Descriptor id);

    void checkRolesAllowCurrentUserToRemove(Descriptor id);
}
