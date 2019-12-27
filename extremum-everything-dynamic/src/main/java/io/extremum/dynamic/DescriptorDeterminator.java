package io.extremum.dynamic;

import io.extremum.sharedmodels.descriptor.Descriptor;

public interface DescriptorDeterminator {
    boolean isDescriptorForDynamicModel(Descriptor id);
}
