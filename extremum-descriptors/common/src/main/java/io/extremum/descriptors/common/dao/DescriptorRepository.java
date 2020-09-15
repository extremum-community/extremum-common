package io.extremum.descriptors.common.dao;

import io.extremum.sharedmodels.descriptor.Descriptor;

import java.util.Optional;

/**
 * @author rpuch
 */
public interface DescriptorRepository {
    Optional<Descriptor> findByExternalId(String externalId);

    Optional<Descriptor> findByInternalId(String internalId);

    Optional<Descriptor> findByCollectionCoordinatesString(String coordinatesString);
}
