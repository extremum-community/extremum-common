package io.extremum.common.support;

import io.extremum.sharedmodels.descriptor.Descriptor;

public interface UniversalReactiveModelLoaders {
    UniversalReactiveModelLoader findLoader(Descriptor descriptor);
}
