package io.extremum.common.support;

import com.google.common.collect.ImmutableList;
import io.extremum.sharedmodels.descriptor.Descriptor;

import java.util.List;

public class ListBasedUniversalReactiveModelLoaders implements UniversalReactiveModelLoaders {
    private final List<UniversalReactiveModelLoader> loaders;

    public ListBasedUniversalReactiveModelLoaders(List<UniversalReactiveModelLoader> loaders) {
        this.loaders = ImmutableList.copyOf(loaders);
    }

    @Override
    public UniversalReactiveModelLoader findLoader(Descriptor descriptor) {
        return loaders.stream()
                .filter(loader -> loader.type() == descriptor.getStorageType())
                .findAny()
                .orElseThrow(() ->
                        new IllegalStateException(
                                String.format("No loader supports storage type '%s'", descriptor.getStorageType())
                        ));
    }
}
