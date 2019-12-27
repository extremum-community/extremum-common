package io.extremum.dynamic;

import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class DefaultReactiveDescriptorDeterminator implements ReactiveDescriptorDeterminator {
    private Set<String> dynamicModelNames = ConcurrentHashMap.newKeySet();

    public void registerDynamicModel(String modelName) {
        dynamicModelNames.add(modelName);
    }

    @Override
    public Mono<Boolean> isDescriptorForDynamicModel(Descriptor id) {
        return id.getModelTypeReactively()
                .map(dynamicModelNames::contains);
    }
}
