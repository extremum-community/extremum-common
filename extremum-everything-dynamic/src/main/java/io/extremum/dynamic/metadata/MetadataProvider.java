package io.extremum.dynamic.metadata;

import io.extremum.dynamic.models.DynamicModel;

public interface MetadataProvider<Model extends DynamicModel<?>> {
    Model provideMetadata(Model model);
}
