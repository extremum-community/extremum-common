package io.extremum.security;

import io.extremum.common.model.Model;

/**
 * @author rpuch
 */
public interface DataSecurity {
    void checkGetAllowed(Model model);

    void checkPatchAllowed(Model model);

    void checkRemovalAllowed(Model model);

    void checkWatchAllowed(Model model);
}
