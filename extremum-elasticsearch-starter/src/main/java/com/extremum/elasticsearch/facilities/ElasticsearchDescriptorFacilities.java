package com.extremum.elasticsearch.facilities;

import com.extremum.sharedmodels.descriptor.Descriptor;

import java.util.UUID;

/**
 * @author rpuch
 */
public interface ElasticsearchDescriptorFacilities {
    Descriptor create(UUID uuid, String modelType);

    Descriptor fromInternalId(UUID uuid);

    Descriptor fromInternalId(String internalId);

    Descriptor fromInternalIdOrNull(String uuid);

    UUID resolve(Descriptor descriptor);
}