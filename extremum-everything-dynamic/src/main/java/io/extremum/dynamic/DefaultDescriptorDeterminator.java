package io.extremum.dynamic;

import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class DefaultDescriptorDeterminator implements DescriptorDeterminator {
    private Set<String> dynamicModelNames = ConcurrentHashMap.newKeySet();

    public void registerDynamicModel(String modelName) {
        dynamicModelNames.add(modelName);
    }

    @Override
    public boolean isDescriptorForDynamicModel(Descriptor id) {
        return dynamicModelNames.contains(id.getModelType());
    }
}
