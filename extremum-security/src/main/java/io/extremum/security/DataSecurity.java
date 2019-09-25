package io.extremum.security;

import io.extremum.sharedmodels.basic.Model;

/**
 * @author rpuch
 */
public interface DataSecurity {
    void checkGetAllowed(Model model);

    void checkPatchAllowed(Model model);

    void checkRemovalAllowed(Model model);

    void checkWatchAllowed(Model model);
}
