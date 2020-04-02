package io.extremum.common.model;

import io.extremum.sharedmodels.descriptor.Descriptor;

public interface HasUuid {
    Descriptor getUuid();

    void setUuid(Descriptor uuid);
}
