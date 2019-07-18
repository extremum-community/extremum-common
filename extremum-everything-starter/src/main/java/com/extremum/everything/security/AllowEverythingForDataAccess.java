package com.extremum.everything.security;

import com.extremum.common.models.Model;

/**
 * @author rpuch
 */
public final class AllowEverythingForDataAccess implements EverythingDataSecurity {
    @Override
    public void checkGetAllowed(Model model) {
        // allow anything
    }

    @Override
    public void checkPatchAllowed(Model model) {
        // allow anything
    }

    @Override
    public void checkRemovalAllowed(Model model) {
        // allow anything
    }
}