package io.extremum.security;

import io.extremum.common.models.Model;

/**
 * @author rpuch
 */
public interface DataSecurity {
    void checkGetAllowed(Model model);

    void checkPatchAllowed(Model model);

    void checkRemovalAllowed(Model model);
}
