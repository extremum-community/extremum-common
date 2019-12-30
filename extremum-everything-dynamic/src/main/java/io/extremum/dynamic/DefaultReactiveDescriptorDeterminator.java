package io.extremum.dynamic;

import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.collect.ImmutableSet.copyOf;

@Slf4j
@RequiredArgsConstructor
public class DefaultReactiveDescriptorDeterminator implements ReactiveDescriptorDeterminator {
    private Set<String> dynamicModelNames = ConcurrentHashMap.newKeySet();

    @Override
    public void registerModelName(String modelName) {
        dynamicModelNames.add(modelName);
        log.info("Registered new model name {}", modelName);
    }

    @Override
    public Mono<Boolean> isDynamic(Descriptor id) {
        return id.getModelTypeReactively()
                .map(dynamicModelNames::contains);
    }

    @Override
    public Set<String> getRegisteredModelNames() {
        return copyOf(dynamicModelNames);
    }
}
