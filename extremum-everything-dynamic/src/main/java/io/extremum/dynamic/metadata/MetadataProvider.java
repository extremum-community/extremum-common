package io.extremum.dynamic.metadata;

import io.extremum.dynamic.models.DynamicModel;

public interface MetadataProvider<M extends DynamicModel<?>> {
    M provideMetadata(M m);
}
