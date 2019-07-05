package com.extremum.sharedmodels.descriptor;

import java.util.Optional;

/**
 * @author rpuch
 */
public interface DescriptorLoader {
    Optional<Descriptor> loadByExternalId(String externalId);

    Optional<Descriptor> loadByInternalId(String internalId);
}
