package com.extremum.common.descriptor.dao;

import com.extremum.common.descriptor.Descriptor;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface DescriptorDao {

    Optional<Descriptor> retrieveByExternalId(String externalId);

    Optional<Descriptor> retrieveByInternalId(String internalId);

    Map<String, String> retrieveMapByExternalIds(Collection<String> externalIds);

    Map<String, String> retrieveMapByInternalIds(Collection<String> internalIds);

    Descriptor store(Descriptor descriptor);
}