package com.extremum.common.descriptor.service;

import com.extremum.sharedmodels.descriptor.Descriptor;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface DescriptorService {

    String createExternalId();

    Descriptor store(Descriptor descriptor);

    Optional<Descriptor> loadByExternalId(String externalId);

    Optional<Descriptor> loadByInternalId(String internalId);

    Map<String, String> loadMapByExternalIds(Collection<String> externalIds);

    Map<String, String> loadMapByInternalIds(Collection<String> internalIds);
}
