package com.extremum.everything.services.management;

import com.extremum.sharedmodels.descriptor.Descriptor;

/**
 * @author rpuch
 */
class NullSecurity implements EverythingSecurity {
    @Override
    public void checkRolesAllowCurrentUserToGet(Descriptor id) {
    }
}
