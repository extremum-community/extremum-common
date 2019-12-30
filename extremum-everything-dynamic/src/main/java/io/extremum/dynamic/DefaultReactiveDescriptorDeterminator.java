package io.extremum.dynamic;

import com.google.common.collect.ImmutableSet;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static reactor.core.publisher.Mono.just;

@Slf4j
@RequiredArgsConstructor
public class DefaultReactiveDescriptorDeterminator implements ReactiveDescriptorDeterminator {
    private Set<String> dynamicModelNames = ConcurrentHashMap.newKeySet();

    @Override
    public Mono<String> registerModelName(String modelName) {
        return Mono.defer(() -> {
            dynamicModelNames.add(modelName);
            log.info("Registered new model name {}", modelName);

            return just(modelName);
        });
    }

    @Override
    public Mono<Boolean> isDynamic(Descriptor id) {
        return id.getModelTypeReactively()
                .map(dynamicModelNames::contains);
    }

    @Override
    public Flux<String> getRegisteredModelNames() {
        return Flux.defer(() -> Flux.fromIterable(ImmutableSet.copyOf(dynamicModelNames)));
    }
}
