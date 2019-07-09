package com.extremum.jpa.facilities;

import com.extremum.sharedmodels.descriptor.Descriptor;

import java.util.UUID;

/**
 * @author rpuch
 */
public interface PostgresDescriptorFacilities {
    Descriptor create(UUID uuid, String modelType);

    Descriptor fromInternalId(UUID uuid);

    Descriptor fromInternalIdOrNull(String uuid);

    UUID resolve(Descriptor descriptor);
}
