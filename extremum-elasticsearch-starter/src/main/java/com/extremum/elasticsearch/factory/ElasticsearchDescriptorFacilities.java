package com.extremum.elasticsearch.factory;

import com.extremum.sharedmodels.descriptor.Descriptor;

import java.util.UUID;

/**
 * @author rpuch
 */
public interface ElasticsearchDescriptorFacilities {
    Descriptor create(UUID uuid, String modelType);

    Descriptor fromInternalId(UUID uuid);

    Descriptor fromInternalIdOrNull(String uuid);

    UUID resolve(Descriptor descriptor);
}
