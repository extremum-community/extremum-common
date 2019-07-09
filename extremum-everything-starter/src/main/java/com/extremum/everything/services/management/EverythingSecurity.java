package com.extremum.everything.services.management;

import com.extremum.sharedmodels.descriptor.Descriptor;

/**
 * @author rpuch
 */
public interface EverythingSecurity {
    void checkRolesAllowCurrentUserToGet(Descriptor id);
}
