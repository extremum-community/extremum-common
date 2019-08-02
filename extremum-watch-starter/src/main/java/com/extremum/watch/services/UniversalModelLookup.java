package com.extremum.watch.services;

import com.extremum.common.models.Model;

/**
 * @author rpuch
 */
public interface UniversalModelLookup {
    Model findModelByInternalId(String internalId);
}
