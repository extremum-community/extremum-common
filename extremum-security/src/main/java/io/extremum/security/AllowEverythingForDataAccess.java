package io.extremum.security;

import io.extremum.sharedmodels.basic.Model;

/**
 * @author rpuch
 */
public class AllowEverythingForDataAccess implements DataSecurity {
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

    @Override
    public void checkWatchAllowed(Model model) {
        // allow anything
    }
}
