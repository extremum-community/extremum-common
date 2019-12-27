package io.extremum.dynamic;

import com.google.common.collect.ImmutableSet;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
public class DefaultReactiveDescriptorDeterminator implements ReactiveDescriptorDeterminator {
    private Set<String> dynamicModelNames = ConcurrentHashMap.newKeySet();

    public void registerDynamicModel(String modelName) {
        dynamicModelNames.add(modelName);
        log.info("Registered new model name {}", modelName);
    }

    @Override
    public Mono<Boolean> isDescriptorForDynamicModel(Descriptor id) {
        return id.getModelTypeReactively()
                .map(dynamicModelNames::contains);
    }

    @Override
    public Set<String> getRegisteredModelNames() {
        return ImmutableSet.copyOf(dynamicModelNames.toArray(new String[]{}));
    }
}
