package com.extremum.security;

import com.extremum.common.models.Model;

/**
 * @author rpuch
 */
public interface EverythingDataSecurity {
    void checkGetAllowed(Model model);

    void checkPatchAllowed(Model model);

    void checkRemovalAllowed(Model model);
}
