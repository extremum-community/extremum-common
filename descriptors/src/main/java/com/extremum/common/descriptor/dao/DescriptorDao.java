package com.extremum.common.descriptor.dao;

import com.extremum.common.descriptor.Descriptor;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface DescriptorDao {

    Optional<Descriptor> retrieveByExternalId(String externalId);

    Optional<Descriptor> retrieveByInternalId(String internalId);

    Map<String, String> retrieveMapByExternalIds(@NotNull Collection<String> externalIds);

    Map<String, String> retrieveMapByInternalIds(Collection<String> internalIds);

    Descriptor store(Descriptor descriptor);
}
