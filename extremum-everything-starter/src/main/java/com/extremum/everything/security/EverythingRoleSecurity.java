package com.extremum.everything.security;

import com.extremum.sharedmodels.descriptor.Descriptor;

/**
 * @author rpuch
 */
public interface EverythingRoleSecurity {
    void checkAllowedGet(Descriptor id);

    void checkAllowedPatch(Descriptor id);

    void checkAllowedRemove(Descriptor id);
}
