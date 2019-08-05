package com.extremum.watch.services;

import com.extremum.common.models.Model;
import org.springframework.stereotype.Service;

/**
 * @author rpuch
 */
// TODO: remove this
@Service
public class UselessUniversalModelLookup implements UniversalModelLookup {
    @Override
    public Model findModelByInternalId(String internalId) {
        throw new UnsupportedOperationException();
    }
}
