package com.extremum.common.descriptor.service;

import com.extremum.common.descriptor.Descriptor;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface DescriptorService {

    Descriptor store(Descriptor descriptor);

    Optional<Descriptor> loadByExternalId(String uuid);

    Optional<Descriptor> loadByInternalId(String internalId);

    Map<String, String> loadMapByExternalIds(Collection<String> externalIds);

    Map<String, String> loadMapByInternalIds(Collection<String> internalIds);
}
