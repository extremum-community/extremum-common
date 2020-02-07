package io.extremum.dynamic.metadata;

import io.extremum.dynamic.models.DynamicModel;

public interface MetadataSupplier {
    boolean isSupports(String modelName);

    <T> void process(DynamicModel<T> model);
}
