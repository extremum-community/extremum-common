package io.extremum.dynamic;

import io.extremum.sharedmodels.descriptor.Descriptor;
import reactor.core.publisher.Mono;

import java.util.Set;

public interface ReactiveDescriptorDeterminator {
    void registerModelName(String modelName);

    Mono<Boolean> isDynamic(Descriptor id);

    Set<String> getRegisteredModelNames();
}
